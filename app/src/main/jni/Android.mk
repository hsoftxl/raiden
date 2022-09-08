LOCAL_PATH := $(call my-dir)

$(call import-add-path,$(COCOS_X_ROOT)/cocos2d-x-3.17.2/)
$(call import-add-path,$(COCOS_X_ROOT)/cocos2d-x-3.17.2/cocos)
$(call import-add-path,$(COCOS_X_ROOT)/cocos2d-x-3.17.2/external)

LOCAL_MODULE := cocos2dcpp_shared

LOCAL_MODULE_FILENAME := libcocos2dcpp

FILE_LIST := hellocpp/main.cpp

FILE_LIST += $(wildcard $(LOCAL_PATH)/Classes/*.cpp)
FILE_LIST += $(wildcard $(LOCAL_PATH)/Classes/*/*.cpp)
FILE_LIST += $(wildcard $(LOCAL_PATH)/Classes/*/*.c)
FILE_LIST += $(wildcard $(LOCAL_PATH)/Classes/*/*/*.cpp)
FILE_LIST += $(wildcard $(LOCAL_PATH)/Classes/*/*/*/*.cpp)

LOCAL_SRC_FILES := hellocpp/md5.c \
                   $(FILE_LIST:$(LOCAL_PATH)/%=%)


LOCAL_C_INCLUDES := hellocpp/md5.h \
                    $(LOCAL_PATH)/Classes

LOCAL_STATIC_LIBRARIES := cc_static

include $(BUILD_SHARED_LIBRARY)

$(call import-module, cocos)
