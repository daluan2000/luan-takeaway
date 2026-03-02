import { spawn } from 'node:child_process';

function resolveMicroValue() {
	const hasNpmMicro = Object.prototype.hasOwnProperty.call(process.env, 'npm_config_micro');
	if (hasNpmMicro) {
		const raw = (process.env.npm_config_micro ?? '').trim().toLowerCase();
		if (raw === '' || raw === 'false' || raw === '0' || raw === 'no' || raw === 'off') {
			return 'false';
		}
		return 'true';
	}

	const micro = (process.env.VITE_IS_MICRO ?? 'true').trim().toLowerCase();
	if (micro === 'false' || micro === '0' || micro === 'no' || micro === 'off') {
		return 'false';
	}
	return 'true';
}

const micro = resolveMicroValue();
const viteArgs = ['--force', ...process.argv.slice(2)];

const child = spawn(
	process.platform === 'win32' ? 'npx.cmd' : 'npx',
	['vite', ...viteArgs],
	{
		stdio: 'inherit',
		env: {
			...process.env,
			VITE_IS_MICRO: micro,
		},
	}
);

child.on('exit', (code, signal) => {
	if (signal) {
		process.kill(process.pid, signal);
		return;
	}
	process.exit(code ?? 0);
});
