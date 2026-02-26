console.log(
    '%cbuild from PakePlus (Optimized for Android): https://github.com/Sjj1024/PakePlus',
    'color:orangered;font-weight:bolder'
);

// 1. 处理链接跳转 (保持原有逻辑，增加安全性)
const hookClick = (e) => {
    const origin = e.target.closest('a');
    const isBaseTargetBlank = document.querySelector('head base[target="_blank"]');
    
    if (!origin) return;

    const href = origin.href;
    
    // 处理特殊协议：电话、邮件、地图等
    if (href && (href.startsWith('tel:') || href.startsWith('mailto:') || href.startsWith('geo:'))) {
        // 允许默认行为，Tauri/Android 会自动拦截并调用原生应用
        console.log('Allowing native protocol:', href);
        return; 
    }

    // 处理外部链接跳转逻辑
    if (
        (href && origin.target === '_blank') ||
        (href && isBaseTargetBlank) ||
        (href && !href.startsWith(window.location.origin) && !href.startsWith('blob:'))
    ) {
        // 防止内部框架死循环，强制在当前页跳转
        e.preventDefault();
        console.log('Intercepted link:', href);
        window.location.href = href;
    }
};

// 2. 重写 window.open (增强兼容性)
const originalOpen = window.open;
window.open = function (url, target, features) {
    if (!url) return null;
    
    // 如果是特殊协议，尝试直接跳转
    if (url.startsWith('tel:') || url.startsWith('mailto:')) {
        window.location.href = url;
        return null;
    }
    
    // 其他情况强制当前页打开，避免安卓 WebView 弹出无法关闭的新窗口
    console.log('Intercepted window.open:', url);
    window.location.href = url;
    return null; 
};

// 3. 【关键】增强文件输入框 (相册/摄像头) 的触发
// 某些安卓 WebView 需要显式触发 click 事件才能拉起系统选择器
const hookFileInput = () => {
    document.addEventListener('click', (e) => {
        const target = e.target;
        if (target.tagName === 'INPUT' && target.type === 'file') {
            console.log('File input clicked, ensuring native picker triggers');
            // 这里不需要额外代码，但保留日志用于调试
            // Tauri v2 应该会自动处理 accept 属性 (如 accept="image/*" 调用相机)
        }
    }, true);
};

// 初始化
document.addEventListener('click', hookClick, { capture: true });
hookFileInput();

console.log('✅ PakePlus Android Optimization Script Loaded');