/*
 * Guillaume Cottenceau (gc at mandrakesoft.com)
 *
 * Copyright 2002 MandrakeSoft
 *
 * This software may be freely redistributed under the terms of the GNU
 * public license.
 *
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdarg.h>

#include <png.h>

#include "roadmap.h"
#include "roadmap_libpng.h"

unsigned char *read_png_file(const char* file_name, int *width, int *height,
                             int *stride) {

   png_byte color_type;
   png_byte bit_depth;

   png_structp png_ptr;
   png_infop info_ptr;
   int number_of_passes;
   png_bytep* row_pointers;
   int y;

   char header[8];   // 8 is the maximum size that can be checked
   unsigned char *buf;
   FILE *fp;

   /* open file and test for it being a png */
   fp = fopen(file_name, "rb");
   if (!fp) return NULL;

   fread(header, 1, 8, fp);
   if (png_sig_cmp(header, 0, 8)) {
      roadmap_log (ROADMAP_ERROR,
         "[read_png_file] File %s is not recognized as a PNG file",
         file_name);
      fclose(fp);
      return NULL;
   }

   /* initialize stuff */
   png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);
   
   if (!png_ptr) {
      roadmap_log (ROADMAP_ERROR,
                  "[read_png_file] png_create_read_struct failed");
      fclose(fp);
      return NULL;
   }

   info_ptr = png_create_info_struct(png_ptr);

   if (!info_ptr) {
      roadmap_log (ROADMAP_ERROR,
            "[read_png_file] png_create_info_struct failed");
      fclose(fp);
      return NULL;
   }

   if (setjmp(png_jmpbuf(png_ptr))) {
      roadmap_log (ROADMAP_ERROR, "[read_png_file] Error during init_io");
      fclose(fp);
      return NULL;
   }

   png_init_io(png_ptr, fp);
   png_set_sig_bytes(png_ptr, 8);

   png_read_info(png_ptr, info_ptr);

   *width = png_get_image_width(png_ptr, info_ptr);
   *height = png_get_image_height(png_ptr, info_ptr);
   *stride = png_get_rowbytes(png_ptr, info_ptr);
   color_type = png_get_color_type(png_ptr, info_ptr);
   bit_depth = png_get_bit_depth(png_ptr, info_ptr);

   number_of_passes = png_set_interlace_handling(png_ptr);
   png_read_update_info(png_ptr, info_ptr);

   /* read file */
   if (setjmp(png_jmpbuf(png_ptr))) {
      roadmap_log (ROADMAP_ERROR, "[read_png_file] Error during read_image");
      fclose(fp);
      return NULL;
   }

   row_pointers = (png_bytep*) malloc(sizeof(png_bytep) * *height);
   buf = malloc (*height * *stride);
   for (y=0; y<*height; y++) {
      row_pointers[y] = (png_byte*) (buf + y * *stride);
   }

   png_read_image(png_ptr, row_pointers);
   png_read_end(png_ptr, NULL);
   png_destroy_read_struct (&png_ptr, &info_ptr, NULL);
   free (row_pointers);

   fclose(fp);

   return buf;
}


