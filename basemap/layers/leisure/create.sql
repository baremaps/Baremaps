-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to you under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_leisure_filtered AS
SELECT
    tags -> 'leisure' AS leisure,
    st_simplifypreservetopology(geom, 78270 / power(2, 12)) AS geom
FROM osm_polygon
WHERE geom IS NOT NULL
  AND st_area(geom) > 78270 / power(2, 12) * 100
  AND tags ->> 'leisure' IN ('garden', 'golf_course', 'marina', 'nature_reserve', 'park', 'pitch', 'sport_center', 'stadium', 'swimming_pool', 'track')
WITH NO DATA;

CREATE INDEX IF NOT EXISTS osm_leisure_filtered_geom_idx ON osm_leisure_filtered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_leisure_filtered_tags_idx ON osm_leisure_filtered (leisure);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_leisure_clustered AS
SELECT
    leisure,
    geom,
    st_clusterdbscan(geom, 0, 0) OVER(PARTITION BY leisure) AS cluster
FROM osm_leisure_filtered
WHERE geom IS NOT NULL
WITH NO DATA;

CREATE INDEX IF NOT EXISTS osm_leisure_clustered_geom_idx ON osm_leisure_clustered USING GIST (geom);
CREATE INDEX IF NOT EXISTS osm_leisure_clustered_tags_idx ON osm_leisure_clustered (leisure);

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_leisure_grouped AS
SELECT
    leisure,
    st_collect(geom) AS geom
FROM osm_leisure_clustered
GROUP BY leisure, cluster
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_leisure_buffered AS
SELECT
    leisure,
    st_buffer(geom, 0, 'join=mitre') AS geom
FROM osm_leisure_grouped
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_leisure_exploded AS
SELECT
    leisure,
    (st_dump(geom)).geom AS geom
FROM osm_leisure_buffered
WITH NO DATA;

CREATE MATERIALIZED VIEW IF NOT EXISTS osm_leisure AS
SELECT
            row_number() OVER () AS id,
            jsonb_build_object('leisure', leisure) AS tags,
            geom
FROM osm_leisure_exploded
WITH NO DATA;
