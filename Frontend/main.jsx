import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import './App.css'
import App from './App.jsx'
import 'bootstrap/dist/css/bootstrap.min.css'

// Add Font Awesome for icons
const fontAwesomeLink = document.createElement('link')
fontAwesomeLink.rel = 'stylesheet'
fontAwesomeLink.href = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css'
document.head.appendChild(fontAwesomeLink)

// Add Google Fonts for better typography
const googleFontsLink = document.createElement('link')
googleFontsLink.rel = 'stylesheet'
googleFontsLink.href = 'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap'
document.head.appendChild(googleFontsLink)

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)