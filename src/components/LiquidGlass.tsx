import { PropsWithChildren, useCallback, useEffect, useRef, type PointerEvent } from 'react'
import { motion, useMotionValue, useSpring } from 'framer-motion'
import html2canvas from 'html2canvas'

const VERTEX_SHADER = `#version 300 es
in vec2 aPosition;
out vec2 vUv;
void main() {
  vUv = aPosition * 0.5 + 0.5;
  gl_Position = vec4(aPosition, 0.0, 1.0);
}`

const FRAGMENT_SHADER = `#version 300 es
precision highp float;

uniform sampler2D uBackdrop;
uniform vec2 uRootSize;
uniform vec2 uElementOrigin;
uniform vec2 uElementSize;
uniform float uRadius;
uniform vec2 uDeformation;
uniform float uDark;
uniform float uTime;

in vec2 vUv;
out vec4 outColor;

float sdRoundRect(vec2 p, vec2 b, float r) {
  vec2 q = abs(p) - b + r;
  return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

float roundedMask(vec2 p, vec2 size, float radius) {
  vec2 center = size * 0.5;
  float d = sdRoundRect(p - center, center - 1.0, radius);
  return 1.0 - smoothstep(-1.2, 0.0, d);
}

vec2 sdfNormal(vec2 p, vec2 size, float radius) {
  float e = 1.25;
  float dx = sdRoundRect(p + vec2(e, 0.0) - size * 0.5, size * 0.5 - 1.0, radius)
           - sdRoundRect(p - vec2(e, 0.0) - size * 0.5, size * 0.5 - 1.0, radius);
  float dy = sdRoundRect(p + vec2(0.0, e) - size * 0.5, size * 0.5 - 1.0, radius)
           - sdRoundRect(p - vec2(0.0, e) - size * 0.5, size * 0.5 - 1.0, radius);
  return normalize(vec2(dx, dy));
}

void main() {
  vec2 p = vUv * uElementSize;
  vec2 center = uElementSize * 0.5;
  float d = sdRoundRect(p - center, center - 1.0, uRadius);
  float mask = 1.0 - smoothstep(-1.5, 0.0, d);

  vec2 normal = sdfNormal(p, uElementSize, uRadius);
  float edge = 1.0 - smoothstep(0.0, 18.0, abs(d));
  float inner = 1.0 - smoothstep(0.0, 42.0, max(d, 0.0));

  // IOR ~ 1.45: stronger optical bend near the curved boundary.
  float eta = 1.0 / 1.45;
  float incidence = clamp(1.0 - abs(dot(normal, normalize(p - center + vec2(0.001)))), 0.0, 1.0);
  float fresnel = pow(1.0 - incidence, 5.0);
  float refractStrength = mix(3.5, 15.0, edge) * (0.72 + fresnel * 0.8);

  // Dragging pulls the virtual liquid surface outward without moving its children.
  vec2 drag = uDeformation;
  float side = smoothstep(0.0, 1.0, edge);
  vec2 elastic = drag * (0.22 + edge * 0.9);
  elastic += normal * dot(drag, normal) * side * 0.35;

  // Slight chromatic separation makes highlights read as glass rather than a flat overlay.
  vec2 basePx = uElementOrigin + p + normal * refractStrength + elastic;
  vec2 uv = basePx / uRootSize;
  vec2 uvR = (basePx + normal * 0.9) / uRootSize;
  vec2 uvB = (basePx - normal * 0.9) / uRootSize;

  vec4 c = texture(uBackdrop, uv);
  vec4 r = texture(uBackdrop, uvR);
  vec4 b = texture(uBackdrop, uvB);
  vec3 refracted = vec3(r.r, c.g, b.b);

  // Clean glass transmission; the shader does not paint a static gradient over the backdrop.
  float transmission = mix(0.92, 0.72, inner);
  vec3 color = mix(c.rgb, refracted, 0.72) * transmission;

  // Diffuse transmission for light mode, stronger Fresnel/specular in dark mode.
  float diffuse = mix(0.12, 0.035, uDark);
  float spec = edge * (0.32 + fresnel * 0.95) * mix(0.85, 1.45, uDark);
  vec3 highlight = vec3(1.0) * spec + vec3(0.9, 0.95, 1.0) * diffuse * (0.5 + edge);

  // Very subtle animated micro-reflection, not a background gradient.
  float glint = pow(max(0.0, sin((vUv.x + vUv.y) * 18.0 + uTime * 0.18)), 32.0) * edge * 0.08;
  color += highlight + glint;

  outColor = vec4(color, mask);
}`

function createProgram(gl: WebGL2RenderingContext) {
  const compile = (type: number, source: string) => {
    const shader = gl.createShader(type)
    if (!shader) throw new Error('Unable to create shader')
    gl.shaderSource(shader, source)
    gl.compileShader(shader)
    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
      throw new Error(gl.getShaderInfoLog(shader) || 'Shader compilation failed')
    }
    return shader
  }

  const vertex = compile(gl.VERTEX_SHADER, VERTEX_SHADER)
  const fragment = compile(gl.FRAGMENT_SHADER, FRAGMENT_SHADER)
  const program = gl.createProgram()
  if (!program) throw new Error('Unable to create WebGL program')
  gl.attachShader(program, vertex)
  gl.attachShader(program, fragment)
  gl.linkProgram(program)
  if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
    throw new Error(gl.getProgramInfoLog(program) || 'Program linking failed')
  }
  gl.deleteShader(vertex)
  gl.deleteShader(fragment)
  return program
}

async function captureBackdrop(root: HTMLElement, glass: HTMLElement) {
  const previous = glass.style.visibility
  glass.style.visibility = 'hidden'
  await new Promise<void>(resolve => requestAnimationFrame(() => resolve()))

  try {
    return await html2canvas(root, {
      backgroundColor: null,
      useCORS: true,
      allowTaint: false,
      logging: false,
      scale: Math.min(window.devicePixelRatio || 1, 1.5),
      imageTimeout: 2500,
    })
  } finally {
    glass.style.visibility = previous
  }
}

export function LiquidGlass({
  children,
  className = '',
  onClick,
  drag = false,
}: PropsWithChildren<{ className?: string; onClick?: () => void; drag?: boolean }>) {
  const ref = useRef<HTMLDivElement>(null)
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const glRef = useRef<WebGL2RenderingContext | null>(null)
  const programRef = useRef<WebGLProgram | null>(null)
  const textureRef = useRef<WebGLTexture | null>(null)
  const rafRef = useRef<number | null>(null)
  const pointerStartRef = useRef<{ x: number; y: number } | null>(null)
  const textureCanvasRef = useRef<HTMLCanvasElement | null>(null)
  const x = useMotionValue(0)
  const y = useMotionValue(0)
  const sx = useSpring(x, { stiffness: 520, damping: 28, mass: 0.48 })
  const sy = useSpring(y, { stiffness: 520, damping: 28, mass: 0.48 })

  const render = useCallback(() => {
    const canvas = canvasRef.current
    const glass = ref.current
    const gl = glRef.current
    const program = programRef.current
    const texture = textureRef.current
    const backdrop = textureCanvasRef.current
    if (!canvas || !glass || !gl || !program || !texture || !backdrop) return

    const dpr = Math.min(window.devicePixelRatio || 1, 1.5)
    const rect = glass.getBoundingClientRect()
    canvas.width = Math.max(1, Math.round(rect.width * dpr))
    canvas.height = Math.max(1, Math.round(rect.height * dpr))
    canvas.style.width = `${rect.width}px`
    canvas.style.height = `${rect.height}px`

    gl.viewport(0, 0, canvas.width, canvas.height)
    gl.useProgram(program)
    gl.bindTexture(gl.TEXTURE_2D, texture)
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, backdrop)

    const root = document.getElementById('root')
    const rootRect = root?.getBoundingClientRect()
    if (!rootRect) return

    gl.uniform2f(gl.getUniformLocation(program, 'uRootSize'), backdrop.width, backdrop.height)
    gl.uniform2f(gl.getUniformLocation(program, 'uElementOrigin'), (rect.left - rootRect.left) * dpr, (rect.top - rootRect.top) * dpr)
    gl.uniform2f(gl.getUniformLocation(program, 'uElementSize'), canvas.width, canvas.height)
    gl.uniform1f(gl.getUniformLocation(program, 'uRadius'), Math.min(30 * dpr, canvas.width * 0.18, canvas.height * 0.18))
    gl.uniform2f(gl.getUniformLocation(program, 'uDeformation'), sx.get() * dpr, sy.get() * dpr)
    gl.uniform1f(gl.getUniformLocation(program, 'uDark'), document.documentElement.classList.contains('dark') ? 1 : 0)
    gl.uniform1f(gl.getUniformLocation(program, 'uTime'), performance.now() / 1000)
    gl.uniform1i(gl.getUniformLocation(program, 'uBackdrop'), 0)

    gl.drawArrays(gl.TRIANGLES, 0, 6)
  }, [sx, sy])

  const refreshBackdrop = useCallback(async () => {
    const root = document.getElementById('root')
    const glass = ref.current
    if (!root || !glass) return
    try {
      textureCanvasRef.current = await captureBackdrop(root, glass)
      render()
    } catch (error) {
      console.warn('[KokoroFy] Liquid Glass backdrop capture failed', error)
    }
  }, [render])

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return
    const gl = canvas.getContext('webgl2', { alpha: true, premultipliedAlpha: false })
    if (!gl) return
    glRef.current = gl

    try {
      const program = createProgram(gl)
      programRef.current = program
      const texture = gl.createTexture()
      textureRef.current = texture
      gl.activeTexture(gl.TEXTURE0)
      gl.bindTexture(gl.TEXTURE_2D, texture)
      gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true)
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR)
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR)
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE)
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE)

      const buffer = gl.createBuffer()
      gl.bindBuffer(gl.ARRAY_BUFFER, buffer)
      gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([
        -1, -1, 1, -1, -1, 1,
        -1, 1, 1, -1, 1, 1,
      ]), gl.STATIC_DRAW)
      const location = gl.getAttribLocation(program, 'aPosition')
      gl.enableVertexAttribArray(location)
      gl.vertexAttribPointer(location, 2, gl.FLOAT, false, 0, 0)

      void refreshBackdrop()
      const onResize = () => { void refreshBackdrop() }
      window.addEventListener('resize', onResize)

      const tick = () => {
        render()
        rafRef.current = requestAnimationFrame(tick)
      }
      rafRef.current = requestAnimationFrame(tick)

      return () => {
        window.removeEventListener('resize', onResize)
        if (rafRef.current) cancelAnimationFrame(rafRef.current)
        gl.deleteTexture(texture)
        gl.deleteProgram(program)
      }
    } catch (error) {
      console.warn('[KokoroFy] WebGL2 Liquid Glass unavailable', error)
    }
  }, [refreshBackdrop, render])

  const onPointerDown = async (event: PointerEvent<HTMLDivElement>) => {
    pointerStartRef.current = { x: event.clientX, y: event.clientY }
    event.currentTarget.setPointerCapture?.(event.pointerId)
    await refreshBackdrop()
  }

  const onPointerMove = (event: PointerEvent<HTMLDivElement>) => {
    const start = pointerStartRef.current
    if (!start) return

    const dx = event.clientX - start.x
    const dy = event.clientY - start.y
    const glass = ref.current
    if (!glass) return

    const rect = glass.getBoundingClientRect()
    const edgeDistance = Math.min(
      event.clientX - rect.left,
      rect.right - event.clientX,
      event.clientY - rect.top,
      rect.bottom - event.clientY
    )

    // Only stretch the liquid when the finger/mouse is close to an edge.
    // The children stay fixed because only the backdrop shader receives this value.
    const edgeFactor = Math.max(0, Math.min(1, (38 - edgeDistance) / 38))
    if (edgeFactor > 0) {
      x.set(Math.max(-60, Math.min(60, dx * 0.48 * edgeFactor)))
      y.set(Math.max(-60, Math.min(60, dy * 0.48 * edgeFactor)))
    }
  }

  const onPointerUp = (event: PointerEvent<HTMLDivElement>) => {
    pointerStartRef.current = null
    event.currentTarget.releasePointerCapture?.(event.pointerId)
    x.set(0)
    y.set(0)
  }

  return (
    <motion.div
      ref={ref}
      className={`liquid-surface ${className}`}
      onClick={onClick}
      onPointerDown={onPointerDown}
      onPointerMove={onPointerMove}
      onPointerUp={onPointerUp}
      onPointerCancel={onPointerUp}
      drag={drag}
      dragConstraints={{ left: -32, right: 32, top: -32, bottom: 32 }}
      dragElastic={0.72}
      dragMomentum={false}
      onDrag={(_, info) => {
        x.set(info.offset.x * 0.22)
        y.set(info.offset.y * 0.22)
      }}
      onDragEnd={() => {
        x.set(0)
        y.set(0)
      }}
      whileTap={{ scale: 0.985 }}
    >
      <canvas ref={canvasRef} className="liquid-backdrop-canvas" aria-hidden="true" />
      <span className="liquid-edge" aria-hidden="true" />
      <span className="relative z-[2]">{children}</span>
    </motion.div>
  )
}
