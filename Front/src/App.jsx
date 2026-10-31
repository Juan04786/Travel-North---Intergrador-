import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Activity, ArrowUpRight, BarChart3, CalendarDays, Compass, MapPin,
  Package, Plus, RefreshCw, Search, Trash2, Users, X,
} from 'lucide-react';
import {
  Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer,
  Legend, Tooltip, XAxis, YAxis,
} from 'recharts';
import { api } from './services/api';

const navigation = [
  { id: 'dashboard', label: 'Resumen', icon: Compass },
  { id: 'destinos', label: 'Destinos', icon: MapPin },
  { id: 'paquetes', label: 'Paquetes', icon: Package },
  { id: 'reservas', label: 'Reservas', icon: CalendarDays },
  { id: 'graficos', label: 'Analítica', icon: BarChart3 },
];

const resourceConfig = {
  destinos: {
    title: 'Destinos', endpoint: 'destinos', singular: 'destino',
    fields: [
      { name: 'codigo', label: 'Código', required: true },
      { name: 'nombre', label: 'Nombre del destino', required: true },
      { name: 'pais', label: 'País', required: true },
      { name: 'ciudad', label: 'Ciudad', required: true },
      { name: 'descripcion', label: 'Descripción', type: 'textarea' },
    ],
    columns: [
      ['codigo', 'Código'], ['nombre', 'Destino'], ['ciudad', 'Ciudad'], ['pais', 'País'],
    ],
  },
  paquetes: {
    title: 'Paquetes turísticos', endpoint: 'paquetes', singular: 'paquete',
    fields: [
      { name: 'nombre', label: 'Nombre del paquete', required: true },
      { name: 'descripcion', label: 'Descripción', type: 'textarea' },
      { name: 'precio', label: 'Precio por persona', type: 'number', min: '0.01', step: '0.01', required: true },
      { name: 'duracionDias', label: 'Duración (días)', type: 'number', min: '1', required: true },
      { name: 'fechaInicio', label: 'Fecha de inicio', type: 'date', required: true },
      { name: 'fechaFin', label: 'Fecha de fin', type: 'date', required: true },
      { name: 'cupoMaximo', label: 'Cupos máximos', type: 'number', min: '1', required: true },
      { name: 'activo', label: 'Estado del paquete', type: 'active' },
      { name: 'destinoIds', label: 'Destinos incluidos', type: 'destinations', required: true },
    ],
    columns: [['nombre', 'Paquete'], ['destinos', 'Destinos'], ['precio', 'Precio'], ['fechaInicio', 'Inicio'], ['cupoMaximo', 'Cupos']],
  },
  reservas: {
    title: 'Reservas', endpoint: 'reservas', singular: 'reserva',
    fields: [
      { name: 'clienteNombre', label: 'Nombre del cliente', required: true },
      { name: 'clienteEmail', label: 'Correo electrónico', type: 'email', required: true },
      { name: 'cantidadPersonas', label: 'Número de personas', type: 'number', min: '1', required: true },
      { name: 'paqueteId', label: 'Paquete turístico', type: 'packages', required: true },
      { name: 'estado', label: 'Estado', type: 'status' },
    ],
    columns: [['clienteNombre', 'Cliente'], ['paqueteNombre', 'Paquete'], ['cantidadPersonas', 'Personas'], ['total', 'Total'], ['estado', 'Estado']],
  },
};

const chartNames = [
  { id: 'reservas-por-destino', label: 'Reservas por destino' },
  { id: 'reservas-por-mes', label: 'Reservas por mes' },
  { id: 'paquetes-mas-reservados', label: 'Paquetes más reservados' },
];
const colors = ['#0891b2', '#0f766e', '#6366f1', '#f59e0b', '#e11d48', '#7c3aed'];
const currency = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

function getInitialForm(resource, item) {
  if (!item) return {};
  if (resource === 'paquetes') {
    return { ...item, destinoIds: item.destinos?.map((destination) => destination.id) ?? [] };
  }
  if (resource === 'reservas') return { ...item, paqueteId: item.paqueteId };
  return { ...item };
}

function App() {
  const [active, setActive] = useState('dashboard');
  const [data, setData] = useState({ destinos: [], paquetes: [], reservas: [] });
  const [charts, setCharts] = useState({});
  const [health, setHealth] = useState('UNKNOWN');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [dialog, setDialog] = useState(null);
  const [saving, setSaving] = useState(false);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [destinos, paquetes, reservas, status] = await Promise.all([
        api.list('destinos'), api.list('paquetes'), api.list('reservas'), api.health(),
      ]);
      setData({ destinos, paquetes, reservas });
      setHealth(status.status);
    } catch (requestError) {
      setError(requestError.message);
      setHealth('DOWN');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(refresh, 0);
    return () => window.clearTimeout(timer);
  }, [refresh]);

  useEffect(() => {
    if (active !== 'graficos') return;
    Promise.all(chartNames.map(async ({ id }) => [id, await api.chart(id)]))
      .then((results) => setCharts(Object.fromEntries(results)))
      .catch((requestError) => setError(`No se pudieron cargar los gráficos: ${requestError.message}`));
  }, [active, data]);

  const config = resourceConfig[active];
  const rows = useMemo(() => {
    if (!config) return [];
    const query = search.trim().toLocaleLowerCase('es');
    return data[active].filter((item) => JSON.stringify(item).toLocaleLowerCase('es').includes(query));
  }, [active, config, data, search]);

  const save = async (event) => {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const values = {};
    for (const [key, value] of formData.entries()) {
      if (key === 'destinoIds') {
        values.destinoIds = formData.getAll(key).map(Number);
      } else if (['precio', 'duracionDias', 'cupoMaximo', 'cantidadPersonas', 'paqueteId'].includes(key)) {
        values[key] = value === '' ? null : Number(value);
      } else if (key === 'activo') {
        values[key] = value === 'true';
      } else if (value !== '') {
        values[key] = value;
      }
    }
    setSaving(true);
    try {
      const { resource, item } = dialog;
      if (item) await api.update(resourceConfig[resource].endpoint, item.id, values);
      else await api.create(resourceConfig[resource].endpoint, values);
      setDialog(null);
      await refresh();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSaving(false);
    }
  };

  const remove = async (resource, item) => {
    if (!window.confirm(`¿Eliminar este registro de ${resourceConfig[resource].singular}?`)) return;
    try {
      await api.remove(resourceConfig[resource].endpoint, item.id);
      await refresh();
    } catch (requestError) {
      setError(requestError.message);
    }
  };

  const section = navigation.find((item) => item.id === active);

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#" onClick={() => setActive('dashboard')}>
          <span className="brand-mark"><Compass size={23} /></span>
          <span><strong>Travel North</strong><small>Panel de gestión</small></span>
        </a>
        <div className="nav-label">GESTIÓN</div>
        <nav>
          {navigation.map(({ id, label, icon: Icon }) => (
            <button key={id} className={`nav-item ${active === id ? 'selected' : ''}`} onClick={() => { setActive(id); setSearch(''); setError(''); }}>
              <Icon size={18} /><span>{label}</span>{active === id && <ArrowUpRight size={15} className="nav-arrow" />}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <span className={`health-dot ${health === 'UP' ? 'online' : ''}`} />
          <span>API {health === 'UP' ? 'conectada' : 'desconectada'}</span>
          <button className="icon-button subtle" onClick={refresh} title="Actualizar datos"><RefreshCw size={15} /></button>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">Travel North <span>/</span> {section?.label ?? 'Resumen'}</div>
          <button className="button button-outline" onClick={refresh} disabled={loading}><RefreshCw size={16} /> Actualizar</button>
        </header>
        <div className="page">
          <div className="page-heading">
            <div>
              <div className="eyebrow">AGENCIA DE VIAJES</div>
              <h1>{section?.label ?? 'Resumen'}</h1>
              <p>{active === 'dashboard' ? 'Una vista general de tus destinos, paquetes y reservas.' : `Administra ${section?.label.toLocaleLowerCase('es') ?? 'los datos del viaje'} desde un solo lugar.`}</p>
            </div>
            {config && <button className="button button-primary" onClick={() => setDialog({ resource: active, item: null })}><Plus size={17} /> Nuevo {config.singular}</button>}
          </div>

          {error && <div className="alert" role="alert"><span>{error}</span><button onClick={() => setError('')} aria-label="Cerrar"><X size={16} /></button></div>}
          {loading && <div className="loading"><span className="spinner" /> Cargando información...</div>}

          {!loading && active === 'dashboard' && (
            <>
              <section className="stats-grid">
                <StatCard label="Destinos" value={data.destinos.length} icon={MapPin} color="cyan" detail="lugares por descubrir" />
                <StatCard label="Paquetes activos" value={data.paquetes.filter((item) => item.activo).length} icon={Package} color="purple" detail="experiencias disponibles" />
                <StatCard label="Reservas" value={data.reservas.length} icon={CalendarDays} color="amber" detail="en todos los paquetes" />
                <StatCard label="Viajeros" value={data.reservas.reduce((sum, item) => sum + item.cantidadPersonas, 0)} icon={Users} color="green" detail="personas registradas" />
              </section>
              <section className="panel">
                <div className="panel-heading"><div><h2>Reservas recientes</h2><p>Últimos registros de clientes</p></div><button className="text-button" onClick={() => setActive('reservas')}>Ver todas <ArrowUpRight size={15} /></button></div>
                <DataTable config={resourceConfig.reservas} rows={data.reservas.slice(-5).reverse()} onEdit={(item) => setDialog({ resource: 'reservas', item })} onDelete={(item) => remove('reservas', item)} />
              </section>
            </>
          )}

          {!loading && config && (
            <section className="panel">
              <div className="panel-heading">
                <div><h2>{config.title} registrados</h2><p>{rows.length} {rows.length === 1 ? 'registro' : 'registros'}</p></div>
                <label className="search-box"><Search size={16} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Buscar..." /></label>
              </div>
              <DataTable config={config} rows={rows} onEdit={(item) => setDialog({ resource: active, item })} onDelete={(item) => remove(active, item)} />
            </section>
          )}

          {!loading && active === 'graficos' && (
            <section className="chart-grid">
              {chartNames.map(({ id, label }, index) => (
                <div className="panel chart-panel" key={id}>
                  <div className="panel-heading"><div><h2>{label}</h2><p>Información procesada por el módulo analítico</p></div><BarChart3 size={19} className="chart-icon" /></div>
                  <ChartView rows={charts[id] ?? []} variant={index} />
                </div>
              ))}
            </section>
          )}
        </div>
      </main>

      {dialog && (
        <FormDialog
          dialog={dialog}
          destinations={data.destinos}
          packages={data.paquetes}
          saving={saving}
          onClose={() => setDialog(null)}
          onSubmit={save}
        />
      )}
    </div>
  );
}

function StatCard({ label, value, icon: Icon, color, detail }) {
  return <article className="stat-card"><div className={`stat-icon ${color}`}><Icon size={19} /></div><div className="stat-label">{label}</div><div className="stat-value">{value}</div><div className="stat-detail">{detail}</div></article>;
}

function DataTable({ config, rows, onEdit, onDelete }) {
  if (rows.length === 0) return <div className="empty-state"><Activity size={25} /><strong>Aún no hay registros</strong><span>Cuando agregues información, aparecerá aquí.</span></div>;
  return (
    <div className="table-scroll"><table><thead><tr>{config.columns.map(([, label]) => <th key={label}>{label}</th>)}<th className="actions-heading">Acciones</th></tr></thead>
      <tbody>{rows.map((row) => <tr key={row.id}>
        {config.columns.map(([key]) => <td key={key}>{renderCell(key, row[key])}</td>)}
        <td className="row-actions"><button className="icon-button edit" onClick={() => onEdit(row)} title="Editar">Editar</button><button className="icon-button delete" onClick={() => onDelete(row)} title="Eliminar"><Trash2 size={15} /></button></td>
      </tr>)}</tbody>
    </table></div>
  );
}

function renderCell(key, value) {
  if (key === 'destinos') return value?.map((destination) => destination.nombre).join(', ') || '—';
  if (key === 'precio' || key === 'total') return currency.format(Number(value) || 0);
  if (key === 'fechaInicio' || key === 'fechaReserva') return value ? new Date(`${value}T00:00:00`).toLocaleDateString('es-CO') : '—';
  if (key === 'estado') return <span className={`status-tag ${String(value).toLowerCase()}`}>{value}</span>;
  return value ?? '—';
}

function FormDialog({ dialog, destinations, packages, saving, onClose, onSubmit }) {
  const { resource, item } = dialog;
  const config = resourceConfig[resource];
  const initial = getInitialForm(resource, item);
  return (
    <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}>
      <form className="modal" onSubmit={onSubmit}>
        <div className="modal-heading"><div><div className="eyebrow">{item ? 'ACTUALIZAR REGISTRO' : 'NUEVO REGISTRO'}</div><h2>{item ? 'Editar' : 'Crear'} {config.singular}</h2></div><button type="button" className="icon-button" onClick={onClose} aria-label="Cerrar"><X size={19} /></button></div>
        <div className="form-grid">
          {config.fields.map((field) => <label className={`form-field ${field.type === 'textarea' || field.type === 'destinations' ? 'wide' : ''}`} key={field.name}>
            <span>{field.label}{field.required && <i> *</i>}</span>
            {field.type === 'textarea'
              ? <textarea name={field.name} defaultValue={initial[field.name] ?? ''} required={field.required} rows="3" />
              : field.type === 'destinations'
                ? <select name={field.name} multiple defaultValue={initial.destinoIds?.map(String) ?? []} required={field.required} size={Math.min(Math.max(destinations.length, 2), 5)}>
                  {destinations.map((destination) => <option key={destination.id} value={destination.id}>{destination.nombre} · {destination.ciudad}</option>)}
                </select>
                : field.type === 'packages'
                  ? <select name={field.name} defaultValue={initial[field.name] ?? ''} required={field.required}><option value="">Selecciona un paquete</option>{packages.map((itemPackage) => <option value={itemPackage.id} key={itemPackage.id}>{itemPackage.nombre}</option>)}</select>
                  : field.type === 'status'
                    ? <select name={field.name} defaultValue={initial[field.name] ?? 'PENDIENTE'}><option value="PENDIENTE">Pendiente</option><option value="CONFIRMADA">Confirmada</option><option value="CANCELADA">Cancelada</option></select>
                      : field.type === 'active'
                        ? <select name={field.name} defaultValue={initial[field.name] ?? true}><option value="true">Activo</option><option value="false">Inactivo</option></select>
                      : <input name={field.name} type={field.type ?? 'text'} defaultValue={initial[field.name] ?? ''} min={field.min} step={field.step} required={field.required} />}
          </label>)}
        </div>
        {resource === 'paquetes' && <p className="form-hint">Para seleccionar varios destinos, mantén presionada la tecla Ctrl (Windows) o Cmd (Mac).</p>}
        <div className="modal-actions"><button type="button" className="button button-outline" onClick={onClose}>Cancelar</button><button className="button button-primary" disabled={saving}>{saving ? 'Guardando...' : item ? 'Guardar cambios' : 'Crear registro'}</button></div>
      </form>
    </div>
  );
}

function ChartView({ rows, variant }) {
  if (rows.length === 0) return <div className="chart-empty">Aún no hay datos para mostrar.</div>;
  const labelKey = Object.keys(rows[0]).find((key) => typeof rows[0][key] === 'string') ?? Object.keys(rows[0])[0];
  const valueKey = Object.keys(rows[0]).find((key) => typeof rows[0][key] === 'number') ?? Object.keys(rows[0])[1];
  if (variant === 1) return (
    <div className="chart-wrap"><ResponsiveContainer width="100%" height="100%"><BarChart data={rows} margin={{ top: 10, right: 12, left: -20, bottom: 4 }}><CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e8eef2" /><XAxis dataKey={labelKey} tick={{ fontSize: 11, fill: '#718096' }} axisLine={false} tickLine={false} /><YAxis allowDecimals={false} tick={{ fontSize: 11, fill: '#718096' }} axisLine={false} tickLine={false} /><Tooltip /><Bar dataKey={valueKey} fill={colors[variant]} radius={[5, 5, 0, 0]} /></BarChart></ResponsiveContainer></div>
  );
  if (variant === 2) return (
    <div className="chart-wrap"><ResponsiveContainer width="100%" height="100%"><BarChart data={rows} layout="vertical" margin={{ top: 4, right: 20, left: 20, bottom: 4 }}><CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#e8eef2" /><XAxis type="number" allowDecimals={false} tick={{ fontSize: 11, fill: '#718096' }} axisLine={false} tickLine={false} /><YAxis type="category" dataKey={labelKey} width={110} tick={{ fontSize: 11, fill: '#718096' }} axisLine={false} tickLine={false} /><Tooltip /><Bar dataKey={valueKey} fill={colors[variant]} radius={[0, 5, 5, 0]} /></BarChart></ResponsiveContainer></div>
  );
  return (
    <div className="chart-wrap"><ResponsiveContainer width="100%" height="100%"><PieChart><Pie data={rows} dataKey={valueKey} nameKey={labelKey} innerRadius={58} outerRadius={95} paddingAngle={3}>{rows.map((row, index) => <Cell key={row[labelKey] ?? index} fill={colors[index % colors.length]} />)}</Pie><Tooltip /><Legend iconType="circle" wrapperStyle={{ fontSize: 11 }} /></PieChart></ResponsiveContainer></div>
  );
}

export default App;
