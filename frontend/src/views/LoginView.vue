<script setup>
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

// --- ✨ 动画配置：轻纱版 ✨ ---
const CONFIG = {
  particleCount: 120,     // 稍微增加数量，让网格更密一些，像织物
  baseSpeed: 0.2,         // 基础漂浮速度：非常慢，营造静谧感
  connectDistance: 160,   // 连线距离
  mouseDistance: 70,     // 鼠标影响范围：像手的拨动范围
  mousePower: 0.9,        // 排斥力度：不需要太大，轻柔一点
  damping: 0.96,          // 阻尼：0.96 比较滑顺，惯性较好
  maxSpeed: 1.5           // 🚀 速度上限：防止粒子被“弹射”得太快，保持优雅
}

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaKey: ''
})

const captchaUrl = ref('')

const fetchCaptcha = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/captcha')
    form.captchaKey = res.data.captchaKey
    captchaUrl.value = res.data.image
    form.captchaCode = ''
  } catch (e) {
    console.error(e)
    ElMessage.error('验证码加载失败')
  }
}

const handleLogin = async () => {
  if(!form.username || !form.password || !form.captchaCode) {
    ElMessage.warning('请填写完整信息')
    return
  }

  loading.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/login', form)
    if (res.data.success) {
      ElMessage.success('登录成功！')
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
      router.push('/student')
    } else {
      ElMessage.error(res.data.message)
      fetchCaptcha()
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('连接服务器失败')
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}

// --- Canvas 动画逻辑 ---
const canvasRef = ref(null)
let ctx = null
let animationFrameId = null
let particles = []
let mouse = { x: null, y: null }

const initCanvas = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  ctx = canvas.getContext('2d')
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  particles = []
  for (let i = 0; i < CONFIG.particleCount; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      // 基础巡航速度 (非常慢)
      baseVx: (Math.random() - 0.5) * CONFIG.baseSpeed,
      baseVy: (Math.random() - 0.5) * CONFIG.baseSpeed,
      // 额外受力速度
      addedVx: 0,
      addedVy: 0,
      originSize: Math.random() * 2 + 1,
      size: Math.random() * 2 + 1
    })
  }
}

const drawParticles = () => {
  if (!ctx) return
  ctx.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height)

  particles.forEach(p => {
    // 1. 物理运动：位置 = 当前位置 + 基础速度 + 额外受力速度
    p.x += p.baseVx + p.addedVx
    p.y += p.baseVy + p.addedVy

    // 2. 边界反弹
    if (p.x < 0 || p.x > canvasRef.value.width) {
      p.baseVx *= -1
      p.addedVx *= -1
    }
    if (p.y < 0 || p.y > canvasRef.value.height) {
      p.baseVy *= -1
      p.addedVy *= -1
    }

    // 3. 鼠标排斥逻辑 (Scatter Effect)
    if (mouse.x != null) {
      // 计算粒子到鼠标的向量 (注意方向：粒子位置 - 鼠标位置 = 指向外部的向量)
      const dx = p.x - mouse.x
      const dy = p.y - mouse.y
      const dist = Math.sqrt(dx * dx + dy * dy)

      if (dist < CONFIG.mouseDistance) {
        // 计算排斥力度：距离越近，力越大
        // 使用余弦平滑过渡，让中心区域最强，边缘平滑衰减
        const force = (CONFIG.mouseDistance - dist) / CONFIG.mouseDistance
        const angle = Math.atan2(dy, dx)

        // 施加排斥力 (向外推)
        p.addedVx += Math.cos(angle) * force * CONFIG.mousePower
        p.addedVy += Math.sin(angle) * force * CONFIG.mousePower
      }
    }

    // 4. 阻尼与速度限制 (优雅的核心)
    p.addedVx *= CONFIG.damping
    p.addedVy *= CONFIG.damping

    // 计算当前额外速度的总大小（勾股定理）
    const currentSpeed = Math.sqrt(p.addedVx * p.addedVx + p.addedVy * p.addedVy)

    // 如果速度超过上限，按比例缩小，保持方向不变
    if (currentSpeed > CONFIG.maxSpeed) {
      const scale = CONFIG.maxSpeed / currentSpeed
      p.addedVx *= scale
      p.addedVy *= scale
    }

    // 5. 绘制粒子
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255, 255, 255, 0.6)'
    ctx.fill()

    // 6. 连线
    particles.forEach(p2 => {
      const dx = p.x - p2.x
      const dy = p.y - p2.y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < CONFIG.connectDistance) {
        ctx.beginPath()
        ctx.strokeStyle = `rgba(255, 255, 255, ${1 - dist / CONFIG.connectDistance})`
        ctx.lineWidth = 0.5
        ctx.moveTo(p.x, p.y)
        ctx.lineTo(p2.x, p2.y)
        ctx.stroke()
      }
    })
  })

  animationFrameId = requestAnimationFrame(drawParticles)
}

const handleResize = () => {
  if(canvasRef.value) {
    canvasRef.value.width = window.innerWidth
    canvasRef.value.height = window.innerHeight
    initCanvas()
  }
}

const handleMouseMove = (e) => {
  mouse.x = e.clientX
  mouse.y = e.clientY
}

const handleMouseLeave = () => {
  mouse.x = null
  mouse.y = null
}

onMounted(() => {
  fetchCaptcha()
  initCanvas()
  drawParticles()
  window.addEventListener('resize', handleResize)
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('mouseout', handleMouseLeave)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('mouseout', handleMouseLeave)
  cancelAnimationFrame(animationFrameId)
})
</script>

<template>
  <div class="login-container">
    <canvas ref="canvasRef" class="login-bg"></canvas>

    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>学生管理系统</h2>
        </div>
      </template>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="123" prefix-icon="Lock" />
        </el-form-item>

        <el-form-item label="验证码">
          <div class="captcha-box">
            <el-input
              v-model="form.captchaCode"
              placeholder="请输入验证码"
              prefix-icon="Key"
              @keyup.enter="handleLogin"
              style="width: 200px; margin-right: 10px;"
            />
            <img
              :src="captchaUrl"
              alt="captcha"
              @click="fetchCaptcha"
              class="captcha-img"
              title="点击刷新"
            />
          </div>
        </el-form-item>

        <el-button type="primary" class="w-100" :loading="loading" @click="handleLogin">登录</el-button>
        <el-button text class="w-100" @click="goToRegister" style="margin-top: 10px;">没有账号？去注册</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(to bottom right, #2c3e50, #4ca1af);
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

.login-card {
  width: 400px;
  border-radius: 12px;
  z-index: 2;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border: none;
}

.card-header h2 { text-align: center; color: #333; margin: 0; letter-spacing: 2px; }
.w-100 { width: 100%; margin-top: 10px; font-size: 16px; padding: 20px 0; letter-spacing: 1px; }
.captcha-box { display: flex; align-items: center; }
.captcha-img { height: 32px; cursor: pointer; border: 1px solid #dcdfe6; border-radius: 4px; }
</style>
