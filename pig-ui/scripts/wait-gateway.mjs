import { readFileSync, existsSync } from 'node:fs';
import { resolve } from 'node:path';

const DEFAULT_GATEWAY = 'http://127.0.0.1:9999';
const DEFAULT_TIMEOUT_MS = 180000;
const INTERVAL_MS = 2000;

function readEnvProxyPath() {
  const envPath = resolve(process.cwd(), '.env.development');
  if (!existsSync(envPath)) {
    return null;
  }

  const content = readFileSync(envPath, 'utf8');
  const match = content.match(/^\s*VITE_ADMIN_PROXY_PATH\s*=\s*(.+)\s*$/m);
  if (!match) {
    return null;
  }

  return match[1].trim().replace(/^['"]|['"]$/g, '');
}

function sleep(ms) {
  return new Promise((resolveSleep) => setTimeout(resolveSleep, ms));
}

async function checkGateway(url) {
  const endpoint = new URL('/auth/code/image?randomStr=healthcheck', url).toString();
  const response = await fetch(endpoint, { method: 'GET' });
  return response.ok;
}

async function main() {
  const gatewayBase = process.env.VITE_ADMIN_PROXY_PATH || readEnvProxyPath() || DEFAULT_GATEWAY;
  const timeoutMs = Number(process.env.WAIT_GATEWAY_TIMEOUT_MS || DEFAULT_TIMEOUT_MS);
  const start = Date.now();

  console.log(`[wait-gateway] 网关探活地址: ${gatewayBase}`);

  while (Date.now() - start < timeoutMs) {
    try {
      const ready = await checkGateway(gatewayBase);
      if (ready) {
        console.log('[wait-gateway] 网关已就绪，开始启动前端...');
        return;
      }
    }
    catch {
      // ignore and retry
    }

    process.stdout.write('.');
    await sleep(INTERVAL_MS);
  }

  console.log('');
  console.error(`[wait-gateway] 超时(${timeoutMs}ms) 仍未就绪，请检查后端与网关日志。`);
  process.exit(1);
}

main();
