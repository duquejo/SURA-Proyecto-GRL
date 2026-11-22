CREATE TABLE grupo_prevencion (
    id UUID PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL
);

CREATE TABLE empresa_afiliada (
    id UUID PRIMARY KEY,
    grupo_prevencion_id UUID NOT NULL REFERENCES grupo_prevencion (id),
    razon_social VARCHAR(200) NOT NULL
);

CREATE TABLE usuario (
    id UUID PRIMARY KEY,
    grupo_prevencion_id UUID REFERENCES grupo_prevencion (id),
    rol VARCHAR(40) NOT NULL CHECK (
        rol IN ('REPRESENTANTE_SST', 'PRESTADOR', 'PROFESIONAL_PREVENCION', 'LIDER_PREVENCION')
    )
);

CREATE TABLE presupuesto_anual (
    id UUID PRIMARY KEY,
    empresa_afiliada_id UUID NOT NULL REFERENCES empresa_afiliada (id),
    vigencia INT NOT NULL CHECK (vigencia > 0),
    monto_asignado NUMERIC(15, 2) NOT NULL CHECK (monto_asignado >= 0),
    saldo_comprometido NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (saldo_comprometido >= 0),
    saldo_ejecutado NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (saldo_ejecutado >= 0),
    saldo_disponible NUMERIC(15, 2) NOT NULL CHECK (saldo_disponible >= 0),
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    CONSTRAINT uq_empresa_vigencia UNIQUE (empresa_afiliada_id, vigencia),
    CONSTRAINT chk_ecuacion_presupuestal CHECK (
        saldo_disponible = monto_asignado - saldo_comprometido - saldo_ejecutado
    )
);

CREATE TABLE plan_trabajo (
    id UUID PRIMARY KEY,
    empresa_afiliada_id UUID NOT NULL REFERENCES empresa_afiliada (id),
    vigencia INT NOT NULL CHECK (vigencia > 0),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('EN_PLANEACION', 'EN_EJECUCION', 'CERRADO')),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_plan_empresa_vigencia UNIQUE (empresa_afiliada_id, vigencia)
);

CREATE TABLE orden_servicio (
    id UUID PRIMARY KEY,
    plan_trabajo_id UUID NOT NULL REFERENCES plan_trabajo (id),
    prestador_id UUID NOT NULL REFERENCES usuario (id),
    profesional_prevencion_id UUID NOT NULL REFERENCES usuario (id),
    valor_total NUMERIC(15, 2) NOT NULL CHECK (valor_total > 0),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PLANEADA', 'EJECUTADA', 'CANCELADA')),
    fecha_programada DATE NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE evaluacion_riesgo (
    id UUID PRIMARY KEY,
    orden_servicio_id UUID NOT NULL UNIQUE REFERENCES orden_servicio (id),
    nivel_riesgo_anterior INT NOT NULL CHECK (nivel_riesgo_anterior >= 0),
    nivel_riesgo_actual INT NOT NULL CHECK (nivel_riesgo_actual >= 0),
    evaluada_en DATE NOT NULL
);

CREATE TABLE auditoria_inmutable (
    id UUID PRIMARY KEY,
    entidad VARCHAR(50) NOT NULL CHECK (entidad IN ('PresupuestoAnual', 'OrdenServicio', 'EvaluacionRiesgo')),
    entidad_id UUID NOT NULL,
    usuario_id UUID NOT NULL REFERENCES usuario (id),
    rol VARCHAR(40) NOT NULL,
    operacion VARCHAR(20) NOT NULL,
    registrado_en TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    valores_anteriores JSONB NOT NULL,
    valores_nuevos JSONB NOT NULL
);

CREATE FUNCTION rechazar_cambio_auditoria_inmutable()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'La auditoria es inmutable';
END;
$$;

CREATE TRIGGER trg_auditoria_inmutable_sin_cambios
BEFORE UPDATE OR DELETE OR TRUNCATE ON auditoria_inmutable
FOR EACH STATEMENT
EXECUTE FUNCTION rechazar_cambio_auditoria_inmutable();

CREATE INDEX idx_empresa_grupo ON empresa_afiliada (grupo_prevencion_id);
CREATE INDEX idx_usuario_grupo ON usuario (grupo_prevencion_id);
CREATE INDEX idx_presupuesto_empresa ON presupuesto_anual (empresa_afiliada_id);
CREATE INDEX idx_plan_empresa ON plan_trabajo (empresa_afiliada_id);
CREATE INDEX idx_orden_plan ON orden_servicio (plan_trabajo_id);
CREATE INDEX idx_orden_prestador ON orden_servicio (prestador_id);
CREATE INDEX idx_orden_profesional ON orden_servicio (profesional_prevencion_id);
CREATE INDEX idx_auditoria_entidad ON auditoria_inmutable (entidad, entidad_id);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unprocessed ON outbox_event (created_at) WHERE processed_at IS NULL;
