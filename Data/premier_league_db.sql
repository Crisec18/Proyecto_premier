DROP DATABASE IF EXISTS premier_league_db;
CREATE DATABASE premier_league_db;
USE premier_league_db;

CREATE TABLE Liga(
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    region VARCHAR(100) NOT NULL
);

CREATE TABLE Equipo (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    estadio VARCHAR(100) NOT NULL,
    annio_fundacion DATE NOT NULL,
    partidos_jugados INT DEFAULT 0,
    partidos_ganados INT DEFAULT 0,
    partidos_perdidos INT DEFAULT 0,
    goles_empatados INT DEFAULT 0,
    goles_a_favor INT DEFAULT 0,
    goles_en_contra INT DEFAULT 0,
    puntos INT DEFAULT 0
);
CREATE TABLE Partido (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fecha DATE NOT NULL,
    id_equipo_local INT NOT NULL,
    id_equipo_visitante INT NOT NULL,
    jornada VARCHAR(50) NOT NULL,
    estadio VARCHAR(50) NOT NULL,
    estado VARCHAR(50) DEFAULT 'Pendiente',
    goles_local INT DEFAULT 0,
    goles_visitante INT DEFAULT 0,
    id_liga INT NOT NULL,
    FOREIGN KEY (id_equipo_local) REFERENCES Equipo(id),
    FOREIGN KEY (id_equipo_visitante) REFERENCES Equipo(id),
    FOREIGN KEY (id_liga) REFERENCES Liga(id),
    CONSTRAINT chk_equipos_diferentes CHECK (id_equipo_local != id_equipo_visitante)
);

ALTER TABLE Liga ADD COLUMN capacidad INT DEFAULT 20;


