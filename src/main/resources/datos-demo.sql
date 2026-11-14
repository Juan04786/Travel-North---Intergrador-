BEGIN;

INSERT INTO destinos (codigo, nombre, pais, ciudad, descripcion)
VALUES
    ('DEST-BOG', 'Bogota', 'Colombia', 'Bogota', 'Capital colombiana, museos y gastronomia local.'),
    ('DEST-MDE', 'Medellin', 'Colombia', 'Medellin', 'Ciudad de la eterna primavera y recorridos culturales.'),
    ('DEST-SMR', 'Santa Marta', 'Colombia', 'Santa Marta', 'Playas del Caribe y naturaleza en el Parque Tayrona.'),
    ('DEST-CUZ', 'Cusco', 'Peru', 'Cusco', 'Historia inca, arquitectura y paisajes andinos.'),
    ('DEST-CUN', 'Cancun', 'Mexico', 'Cancun', 'Playas caribenas, cenotes y sitios arqueologicos.'),
    ('DEST-BCN', 'Barcelona', 'Espana', 'Barcelona', 'Arquitectura, cultura y costa mediterranea.')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO paquetes_turisticos
    (nombre, descripcion, precio, duracion_dias, fecha_inicio, fecha_fin, cupo_maximo, activo)
VALUES
    ('Colombia Cultural: Bogota y Medellin', 'Recorrido urbano por dos ciudades colombianas.', 1850000.00, 6, '2027-01-15', '2027-01-20', 20, TRUE),
    ('Caribe Colombiano: Santa Marta', 'Descanso en la costa y visita guiada al Parque Tayrona.', 1420000.00, 5, '2027-02-10', '2027-02-14', 18, TRUE),
    ('Aventura en Peru: Cusco y Valle Sagrado', 'Experiencia cultural y de naturaleza en los Andes.', 3250000.00, 8, '2027-03-05', '2027-03-12', 16, TRUE),
    ('Sol y Playa en Cancun', 'Estadia junto al mar con excursiones opcionales.', 2980000.00, 7, '2027-04-12', '2027-04-18', 24, TRUE),
    ('Europa Esencial: Barcelona', 'Viaje cultural por Barcelona y sus alrededores.', 5890000.00, 6, '2027-05-03', '2027-05-08', 14, TRUE)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO paquete_destino (paquete_id, destino_id)
SELECT p.id, d.id
FROM (VALUES
    ('Colombia Cultural: Bogota y Medellin', 'DEST-BOG'),
    ('Colombia Cultural: Bogota y Medellin', 'DEST-MDE'),
    ('Caribe Colombiano: Santa Marta', 'DEST-SMR'),
    ('Aventura en Peru: Cusco y Valle Sagrado', 'DEST-CUZ'),
    ('Sol y Playa en Cancun', 'DEST-CUN'),
    ('Europa Esencial: Barcelona', 'DEST-BCN')
) AS relacion(paquete_nombre, destino_codigo)
JOIN paquetes_turisticos p ON p.nombre = relacion.paquete_nombre
JOIN destinos d ON d.codigo = relacion.destino_codigo
WHERE NOT EXISTS (
    SELECT 1
    FROM paquete_destino existente
    WHERE existente.paquete_id = p.id
      AND existente.destino_id = d.id
);

INSERT INTO reservas
    (cliente_nombre, cliente_email, fecha_reserva, cantidad_personas, total, estado, paquete_id)
SELECT reserva.cliente_nombre,
       reserva.cliente_email,
       CURRENT_DATE,
       reserva.cantidad_personas,
       p.precio * reserva.cantidad_personas,
       reserva.estado,
       p.id
FROM (VALUES
    ('Laura Gomez', 'laura.gomez@example.com', 2, 'CONFIRMADA', 'Colombia Cultural: Bogota y Medellin'),
    ('Andres Ruiz', 'andres.ruiz@example.com', 3, 'PENDIENTE', 'Colombia Cultural: Bogota y Medellin'),
    ('Sofia Martinez', 'sofia.martinez@example.com', 2, 'CONFIRMADA', 'Caribe Colombiano: Santa Marta'),
    ('Daniel Torres', 'daniel.torres@example.com', 4, 'CANCELADA', 'Caribe Colombiano: Santa Marta'),
    ('Valentina Perez', 'valentina.perez@example.com', 2, 'CONFIRMADA', 'Aventura en Peru: Cusco y Valle Sagrado'),
    ('Mateo Castro', 'mateo.castro@example.com', 1, 'PENDIENTE', 'Aventura en Peru: Cusco y Valle Sagrado'),
    ('Camila Herrera', 'camila.herrera@example.com', 3, 'CONFIRMADA', 'Sol y Playa en Cancun'),
    ('Nicolas Rojas', 'nicolas.rojas@example.com', 2, 'PENDIENTE', 'Sol y Playa en Cancun'),
    ('Isabella Diaz', 'isabella.diaz@example.com', 2, 'CONFIRMADA', 'Europa Esencial: Barcelona'),
    ('Samuel Vargas', 'samuel.vargas@example.com', 1, 'CANCELADA', 'Europa Esencial: Barcelona')
) AS reserva(cliente_nombre, cliente_email, cantidad_personas, estado, paquete_nombre)
JOIN paquetes_turisticos p ON p.nombre = reserva.paquete_nombre
WHERE NOT EXISTS (
    SELECT 1
    FROM reservas existente
    WHERE existente.cliente_email = reserva.cliente_email
      AND existente.paquete_id = p.id
);

COMMIT;
