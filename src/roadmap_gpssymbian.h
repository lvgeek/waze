/* roadmap_gpssymbian.h - a module to interact with Symbian location API.
 *
 * LICENSE:
 *
 *   Copyright 2008 Giant Steps Ltd
 *   Base on code Copyright 2007 Ehud Shabtai
 *
 *   This file is part of RoadMap.
 *
 *   RoadMap is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   RoadMap is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with RoadMap; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 */

#ifndef INCLUDE__ROADMAP_GPSSYMBIAN__H
#define INCLUDE__ROADMAP_GPSSYMBIAN__H

typedef void* RoadMapGps;

#include "roadmap_net.h"
#include "roadmap_gps.h"
#include "roadmap_input.h"

void roadmap_gpssymbian_open();
void roadmap_gpssymbian_shutdown();

void roadmap_gpssymbian_subscribe_to_navigation (RoadMapGpsdNavigation navigation);

void roadmap_gpssymbian_subscribe_to_satellites (RoadMapGpsdSatellite satellite);

void roadmap_gpssymbian_subscribe_to_dilution   (RoadMapGpsdDilution dilution);

int roadmap_gpssymbian_decode (void *user_context,
                                void *decoder_context, char *sentence, int length);

#endif // INCLUDE__ROADMAP_GPSSYMBIAN__H

