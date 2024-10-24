--
-- Name: ra_resource; Type: TABLE; Schema: public; Owner: supra
--

CREATE TABLE public.ra_resource (
    id character varying(128) NOT NULL,
    name character varying(128) NOT NULL,
    icon character varying(128) NOT NULL,
    info text NOT NULL,
    create_time timestamp without time zone NOT NULL,
    update_time timestamp without time zone NOT NULL
);

ALTER TABLE public.ra_resource OWNER TO supra;

ALTER TABLE ONLY public.ra_resource
    ADD CONSTRAINT ra_resource_pkey PRIMARY KEY (id);

--
-- Name: ra_session; Type: TABLE; Schema: public; Owner: supra
--

CREATE TABLE public.ra_session (
    id character varying(128) NOT NULL,
    resource_id character varying(128) NOT NULL,
    info text NOT NULL,
    create_time timestamp without time zone NOT NULL,
    update_time timestamp without time zone NOT NULL
);

ALTER TABLE public.ra_session OWNER TO supra;

ALTER TABLE ONLY public.ra_session
    ADD CONSTRAINT ra_session_pkey PRIMARY KEY (id);

--
-- Name: ra_task; Type: TABLE; Schema: public; Owner: supra
--

CREATE TABLE public.ra_task (
    id serial,
    category character varying(64) NOT NULL,
    action character varying(128) NOT NULL,
    status integer NOT NULL,
    info text NOT NULL,
    create_time timestamp without time zone NOT NULL,
    update_time timestamp without time zone NOT NULL
);


ALTER TABLE public.ra_task OWNER TO supra;

ALTER TABLE ONLY public.ra_task
    ADD CONSTRAINT ra_task_pkey PRIMARY KEY (id);

--
-- Name: ra_vm_template; Type: TABLE; Schema: public; Owner: supra
--

CREATE TABLE public.ra_vm_template (
    id character varying(128) NOT NULL,
    name character varying(128) NOT NULL,
    os_type character varying(64) NOT NULL,
    disk_size character varying(64) NOT NULL,
    system_info text NOT NULL,
    create_time timestamp without time zone NOT NULL,
    update_time timestamp without time zone NOT NULL
);

ALTER TABLE public.ra_vm_template OWNER TO supra;

ALTER TABLE ONLY public.ra_vm_template
    ADD CONSTRAINT ra_vm_template_pkey PRIMARY KEY (id);
