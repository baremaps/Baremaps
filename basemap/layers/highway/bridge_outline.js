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
import {asLayerObject, withSortKeys} from "../../utils/utils.js";
import theme from "../../theme.js";

let directives = [
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'motorway'],
            ['==', ['get', 'highway'], 'motorway_link'],
        ],
        'line-color': theme.bridgeOutlineMotorwayLineColor,
        'line-gap-width-stops': theme.highwayMotorwayLineWidth,
        'line-width': 1,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'trunk'],
            ['==', ['get', 'highway'], 'trunk_link'],
        ],
        'line-color': theme.bridgeOutlineTrunkLineColor,
        'line-gap-width-stops': theme.highwayTrunkLineWidth,
        'line-width': 1,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'primary'],
            ['==', ['get', 'highway'], 'primary_link'],
        ],
        'line-color': theme.bridgeOutlinePrimaryLineColor,
        'line-gap-width-stops': theme.highwayPrimaryLineWidth,
        'line-width': 1,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'secondary'],
            ['==', ['get', 'highway'], 'secondary_link'],
        ],
        'line-color': theme.bridgeOutlineSecondaryLineColor,
        'line-gap-width-stops': theme.highwaySecondaryLineWidth,
        'line-width': 1,
    },
    {
        filter: [
            'any',
            ['==', ['get', 'highway'], 'tertiary'],
            ['==', ['get', 'highway'], 'tertiary_link'],
        ],
        'line-color': theme.bridgeOutlineTertiaryLineColor,
        'line-gap-width-stops': theme.highwayTertiaryLineWidth,
        'line-width': 1,
    },
    {
        filter: ['==', ['get', 'highway'], 'unclassified'],
        'line-color': theme.bridgeOutlineUnclassifiedLineColor,
        'line-gap-width-stops': theme.highwayUnclassifiedLineWidth,
        'line-width': 1,
    },
    {
        filter: ['==', ['get', 'highway'], 'residential'],
        'line-color': theme.bridgeOutlineResidentialLineColor,
        'line-gap-width-stops': theme.highwayResidentialLineWidth,
        'line-width': 1,
    },
    {
        filter: ['==', ['get', 'highway'], 'living_street'],
        'line-color': theme.bridgeOutlineLivingStreetLineColor,
        'line-gap-width-stops': theme.highwayLivingStreetLineWidth,
        'line-width': 1,
    },
    {
        filter: ['==', ['get', 'highway'], 'service'],
        'line-color': theme.bridgeOutlineServiceLineColor,
        'line-gap-width-stops': theme.highwayServiceLineWidth,
        'line-width': 1,
    },
    {
        filter: [
            'all',
            ['==', ['get', 'highway'], 'pedestrian'],
            ['!=', ['geometry-type'], 'Polygon'],
        ],
        'line-color': theme.bridgeOutlinePedestrianLineColor,
        'line-gap-width-stops': theme.highwayPedestrianLineWidth,
        'line-width': 1,
    },
];

export default asLayerObject(withSortKeys(directives), {
    id: 'bridge_outline',
    source: 'baremaps',
    'source-layer': 'highway',
    type: 'line',
    layout: {
        visibility: 'visible',
        'line-cap': 'butt',
        'line-join': 'miter',
    },
    filter: ['all',
        ['==', ['geometry-type'], 'LineString'],
        ['==', ['get', 'bridge'], 'yes']
    ],
});
