LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := native-p2p
LOCAL_SRC_FILES := \
    $(subst $(LOCAL_PATH)/,,$(wildcard $(LOCAL_PATH)/p2p/natproxy/*.cpp))  \
    $(subst $(LOCAL_PATH)/,,$(wildcard $(LOCAL_PATH)/p2p/src/*.cpp))

LOCAL_C_INCLUDES += $(LOCAL_PATH)/p2p/inc
LOCAL_C_INCLUDES += $(LOCAL_PATH)/p2p/natproxy
LOCAL_CPPFLAGS :=  -fexceptions
LOCAL_CFLAGS := -DANDROID
LOCAL_LDLIBS := -llog
include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE    := p2pclient-jni
LOCAL_SRC_FILES := P2PclientJni.cpp
LOCAL_CPPFLAGS :=  -fexceptions
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog

LOCAL_C_INCLUDES += $(LOCAL_PATH)/p2p/inc
LOCAL_C_INCLUDES += $(LOCAL_PATH)/p2p/natproxy
LOCAL_STATIC_LIBRARIES := native-p2p
include $(BUILD_SHARED_LIBRARY)