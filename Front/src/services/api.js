const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
  || (import.meta.env.DEV ? '/api' : 'https://travel-north-intergrador-1.onrender.com/api');
async function request(path, options = {}, baseUrl = API_BASE_URL) {
  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
  });

  if (!response.ok) {
    let message = `Error HTTP ${response.status}`;
    try {
      const body = await response.clone().json();
      message = body.error || body.message || message;
    } catch {
      const text = await response.text().catch(() => '');
      if (text) message = text;
    }
    throw new Error(message);
  }

  if (response.status === 204) return null;
  return response.json();
}

const json = (method, body) => ({ method, body: JSON.stringify(body) });

export const api = {
  health: () => request('/health/status'),
  list: (resource) => request(`/${resource}`),
  create: (resource, data) => request(`/${resource}`, json('POST', data)),
  update: (resource, id, data) => request(`/${resource}/${id}`, json('PUT', data)),
  remove: (resource, id) => request(`/${resource}/${id}`, { method: 'DELETE' }),
  chart: (name) => request(`/graficos/headless/${name}`),
};
