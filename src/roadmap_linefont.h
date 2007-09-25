/* roadmap_linefont.h - draw text on a map.
 *
 * LICENSE:
 *
 *   Copyright 2006 Paul G. Fox
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
 */

void roadmap_linefont_extents
        (const char *text, int size,
         int *width, int *ascent, int *descent, int *can_tilt);

void roadmap_linefont_text
        ( RoadMapGuiPoint *center, int where, int size, const char *text);

void roadmap_linefont_text_angle
        ( RoadMapGuiPoint *start, RoadMapGuiPoint *center,
                int theta, int size, const char *text);

/* reuse roadmap canvas values, so callers won't care which text functions
 * are actually eventually called */
#define ROADMAP_LINEFONT_LEFT     ROADMAP_CANVAS_LEFT
#define ROADMAP_LINEFONT_RIGHT    ROADMAP_CANVAS_RIGHT
#define ROADMAP_LINEFONT_TOP      ROADMAP_CANVAS_TOP
#define ROADMAP_LINEFONT_BOTTOM   ROADMAP_CANVAS_BOTTOM
#define ROADMAP_LINEFONT_CENTER_X ROADMAP_CANVAS_CENTER_X
#define ROADMAP_LINEFONT_CENTER_Y ROADMAP_CANVAS_CENTER_Y

#define ROADMAP_LINEFONT_CENTERED \
        (ROADMAP_LINEFONT_CENTER_X | ROADMAP_LINEFONT_CENTER_Y)
#define ROADMAP_LINEFONT_CENTERED_ABOVE \
        (ROADMAP_LINEFONT_CENTER_X | ROADMAP_LINEFONT_BOTTOM)
