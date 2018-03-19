/*
  Copyright (C) 1997-2017 Sam Lantinga <slouken@libsdl.org>

  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely.
*/
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

#include "SDL_test_common.h"

#if defined(__IPHONEOS__) || defined(__ANDROID__)
#define HAVE_OPENGLES
#endif

#ifdef HAVE_OPENGLES

#include "SDL_opengles.h"
#include "../SDL_image/SDL_image.h"
#include "../SDL/include/SDL.h"
#include "../SDL/include/SDL_render.h"


static char *icon_xpm[] = {
        "32 23 3 1",
        "     c #FFFFFF",
        ".    c #000000",
        "+    c #FFFF00",
        "                                ",
        "            ........            ",
        "          ..++++++++..          ",
        "         .++++++++++++.         ",
        "        .++++++++++++++.        ",
        "       .++++++++++++++++.       ",
        "      .++++++++++++++++++.      ",
        "      .+++....++++....+++.      ",
        "     .++++.. .++++.. .++++.     ",
        "     .++++....++++....++++.     ",
        "     .++++++++++++++++++++.     ",
        "     .++++++++++++++++++++.     ",
        "     .+++++++++..+++++++++.     ",
        "     .+++++++++..+++++++++.     ",
        "     .++++++++++++++++++++.     ",
        "      .++++++++++++++++++.      ",
        "      .++...++++++++...++.      ",
        "       .++............++.       ",
        "        .++..........++.        ",
        "         .+++......+++.         ",
        "          ..++++++++..          ",
        "            ........            ",
        "                                "};


int
main(int argc, char *argv[]) {

    SDL_Window *window;
    SDL_Renderer *renderer;
    SDL_Surface *surface;
    SDL_Texture *texture;
    int done;
    SDL_Event event;

    if (SDL_CreateWindowAndRenderer(0, 0, 0, &window, &renderer) < 0) {
        SDL_LogError(SDL_LOG_CATEGORY_APPLICATION,
                     "SDL_CreateWindowAndRenderer() failed: %s", SDL_GetError());
        return (2);
    }

    surface = IMG_ReadXPMFromArray(icon_xpm);
    texture = SDL_CreateTextureFromSurface(renderer, surface);
    if (!texture) {
        SDL_LogError(SDL_LOG_CATEGORY_APPLICATION,
                     "Couldn't load texture: %s", SDL_GetError());
        return (2);
    }
    SDL_SetWindowSize(window, 200, 200);

    done = 0;
    while (!done) {
        while (SDL_PollEvent(&event)) {
            if (event.type == SDL_QUIT)
                done = 1;
        }
        SDL_RenderCopy(renderer, texture, NULL, NULL);
        SDL_RenderPresent(renderer);
        SDL_Delay(100);
    }
    SDL_DestroyTexture(texture);

    SDL_Quit();
    return (0);

#if !defined(__ANDROID__)
    quit(0);
#endif
    return 0;
}

#else /* HAVE_OPENGLES */

int
main(int argc, char *argv[])
{
    SDL_LogError(SDL_LOG_CATEGORY_APPLICATION, "No OpenGL ES support on this system\n");
    return 1;
}

#endif /* HAVE_OPENGLES */

/* vi: set ts=4 sw=4 expandtab: */
