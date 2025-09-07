import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  /* config options here */
  experimental: {
    ppr: 'incremental' // 일부 라우트에서 PPR을 사용할 수 있도록 설정
  }
};

export default nextConfig;
