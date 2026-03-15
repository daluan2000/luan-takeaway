const API_BASE_URL = import.meta.env.VITE_API_URL || '';

const LOCAL_HOSTS = new Set(['localhost', '127.0.0.1', '0.0.0.0', 'host.docker.internal']);

const joinUrl = (prefix: string, path: string) => {
	if (!prefix) return path;
	return `${prefix.replace(/\/+$/, '')}/${path.replace(/^\/+/, '')}`;
};

const normalizeDuplicatedBasePrefix = (path: string, baseURL: string) => {
	if (!path || !baseURL) return path;
	if (!baseURL.startsWith('/')) return path;
	const normalizedBase = baseURL.replace(/\/+$/, '');
	const duplicatedPrefix = `${normalizedBase}${normalizedBase}/`;
	if (path.startsWith(duplicatedPrefix)) {
		return path.slice(normalizedBase.length);
	}
	return path;
};

const shouldRewriteAbsoluteUrl = (url: URL, baseURL: string) => {
	if (!baseURL || !baseURL.startsWith('/')) return false;
	if (LOCAL_HOSTS.has(url.hostname)) return true;
	if (typeof window !== 'undefined' && url.hostname === window.location.hostname) return true;
	return /^\/(api\/)?admin\//.test(url.pathname);
};

const resolveAbsoluteUrl = (absoluteUrl: string, baseURL: string) => {
	let url: URL;
	try {
		url = new URL(absoluteUrl);
	} catch {
		return absoluteUrl;
	}

	if (!shouldRewriteAbsoluteUrl(url, baseURL)) {
		return absoluteUrl;
	}

	const pathWithQuery = `${url.pathname}${url.search}${url.hash}`;
	const sanitizedPath = normalizeDuplicatedBasePrefix(pathWithQuery, baseURL);
	if (baseURL && sanitizedPath.startsWith(baseURL)) return sanitizedPath;
	return joinUrl(baseURL, sanitizedPath);
};

/**
 * Resolve API resource URL from backend-returned path.
 * Supports absolute urls/data/blob, and joins relative paths with VITE_API_URL.
 */
export function resolveApiResourceUrl(path?: string, baseURL = API_BASE_URL): string {
	if (!path) return '';
	const normalized = path.trim();
	if (/^https?:\/\//i.test(normalized)) {
		return resolveAbsoluteUrl(normalized, baseURL);
	}
	if (/^\/\//.test(normalized) || normalized.startsWith('data:') || normalized.startsWith('blob:')) return normalized;
	const firstPath = normalized.split(',')[0]?.trim() || normalized;
	const sanitizedPath = normalizeDuplicatedBasePrefix(firstPath, baseURL);
	if (baseURL && sanitizedPath.startsWith(baseURL)) return sanitizedPath;
	return joinUrl(baseURL, sanitizedPath);
}
