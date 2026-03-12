const API_BASE_URL = import.meta.env.VITE_API_URL || '';

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

/**
 * Resolve API resource URL from backend-returned path.
 * Supports absolute urls/data/blob, and joins relative paths with VITE_API_URL.
 */
export function resolveApiResourceUrl(path?: string, baseURL = API_BASE_URL): string {
	if (!path) return '';
	const normalized = path.trim();
	if (/^(https?:)?\/\//i.test(normalized) || normalized.startsWith('data:') || normalized.startsWith('blob:')) return normalized;
	const firstPath = normalized.split(',')[0]?.trim() || normalized;
	const sanitizedPath = normalizeDuplicatedBasePrefix(firstPath, baseURL);
	if (baseURL && sanitizedPath.startsWith(baseURL)) return sanitizedPath;
	return joinUrl(baseURL, sanitizedPath);
}
