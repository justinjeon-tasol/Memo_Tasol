import axios from 'axios';

const api = axios.create({
    // 환경변수(NEXT_PUBLIC_API_BASE_URL)를 사용합니다.
    // 개발 시에는 .env.local 파일에 설정하세요.
    baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:3001',
});

api.interceptors.request.use((config) => {
    if (typeof window !== 'undefined') {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;
