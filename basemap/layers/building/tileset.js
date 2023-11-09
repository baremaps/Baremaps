/**
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to you under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **/

export default {
    id: 'building',
    queries: [
        {
            minzoom: 13,
            maxzoom: 20,
            sql: `
                SELECT 
                    id, 
                    tags
                        || jsonb_build_object('extrusion:base', (CASE
                                                                     WHEN tags ? 'building:min_height'
                                                                         THEN tags ->> 'building:min_height'
                                                                     WHEN tags ->> 'building:min_level' ~ '^[0-9\\\\\\\\.]+$'
                                                                         THEN tags ->> 'building:min_level'
                                                                     ELSE '0' END)::real * 3)
                        || jsonb_build_object('extrusion:height', (CASE
                                                                   WHEN tags ? 'building:height'
                                                                       THEN tags ->> 'building:height'
                                                                   WHEN tags ->> 'building:levels' ~ '^[0-9\\\\\\\\.]+$'
                                                                       THEN tags ->> 'building:levels'
                                                                   ELSE '2' END)::real * 3) as tags,
                    geom 
                FROM osm_ways 
                WHERE tags ? 'building'`,
        },
        {
            minzoom: 13,
            maxzoom: 20,
            sql: `
                SELECT 
                    id, 
                    tags 
                        || jsonb_build_object('extrusion:base', (CASE
                                                                      WHEN tags ? 'building:min_height'
                                                                          THEN tags ->> 'building:min_height'
                                                                      WHEN tags ->> 'building:min_level' ~ '^[0-9\\\\\\\\.]+$'
                                                                          THEN tags ->> 'building:min_level'
                                                                      ELSE '0' END)::real * 3)
                        || jsonb_build_object('extrusion:height', (CASE
                                                                   WHEN tags ? 'building:height'
                                                                       THEN tags ->> 'building:height'
                                                                   WHEN tags ->> 'building:levels' ~ '^[0-9\\\\\\\\.]+$'
                                                                       THEN tags ->> 'building:levels'
                                                                   ELSE '2' END)::real * 3) as tags,
                    geom 
                FROM osm_relations 
                WHERE tags ? 'building'`,
        },
    ],
}
