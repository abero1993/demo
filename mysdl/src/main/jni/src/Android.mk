LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := main

SDL_PATH := ../SDL
SDL_IMAGE_PATH=../SDL_image

LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(SDL_PATH)/include \  $(LOCAL_PATH)/$(SDL_IMAGE_PATH)

# Add your application source files here...
LOCAL_SRC_FILES :=  testgles.c

LOCAL_SHARED_LIBRARIES := SDL2  SDL2_image


LOCAL_LDLIBS := -lGLESv1_CM -lGLESv2 -llog

include $(BUILD_SHARED_LIBRARY)
