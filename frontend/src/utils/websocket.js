import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

let stompClient = null
let reconnectAttempts = 0
const MAX_RECONNECT_ATTEMPTS = 5

export function connect(userId, onMessage) {
  if (stompClient && stompClient.connected) {
    return
  }

  const socket = new SockJS('http://localhost:8080/ws')

  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,

    onConnect: () => {
      reconnectAttempts = 0

      // 订阅用户专属通知队列
      stompClient.subscribe(`/user/${userId}/queue/notifications`, (message) => {
        const notification = JSON.parse(message.body)
        if (onMessage) {
          onMessage(notification)
        }
      })
    },

    onStompError: (frame) => {
      console.error('STOMP 错误:', frame.headers['message'])
    },

    onWebSocketClose: () => {
      if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        reconnectAttempts++
      }
    }
  })

  stompClient.activate()
}

export function disconnect() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
}

export function isConnected() {
  return stompClient && stompClient.connected
}
