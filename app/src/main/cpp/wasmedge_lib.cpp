#include <wasmedge/wasmedge.h>
#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

extern "C" JNIEXPORT jstring JNICALL
        JNIEnv *env,
        jobject /* this */,
        jbyteArray imageData,
        jint width,
        jint height) {
    WasmEdge_ConfigureContext *conf = WasmEdge_ConfigureCreate();
    WasmEdge_VMContext *vm = WasmEdge_VMCreate(vm, conf);

    const char* wasm_file = "/data/data/diploma.pr.biovote/files/my_project.wasm";
    WasmEdge_String wasi_dir = WasmEdge_StringCreateByCString("/data/data/diploma.pr.biovote/files");
    WasmEdge_String wasi_map_dir = WasmEdge_StringCreateByCString(".");
    WasmEdge_LoaderContext *loader = WasmEdge_LoaderCreate(nullptr);
    WasmEdge_StoreContext *store = WasmEdge_StoreCreate();
    WasmEdge_ASTModuleContext *ast_mod = WasmEdge_LoaderParseFromFile(loader, store, wasm_file);
    WasmEdge_ModuleInstanceContext *mod = WasmEdge_ExecutorInstantiate(loader, store, ast_mod, nullptr);

    jbyte* bytes = env->GetByteArrayElements(imageData, 0);
    size_t len = env->GetArrayLength(imageData);

    WasmEdge_MemoryInstance *mem = WasmEdge_ModuleFindMemory(mod, "memory");
    uint32_t offset = WasmEdge_MemoryInstanceAlloc(mem, len);
    uint8_t *mem_data = (uint8_t*)WasmEdge_MemoryInstanceGetData(mem);
    memcpy(mem_data + offset, bytes, len);

    WasmEdge_FunctionInstance *func = WasmEdge_ModuleFindFunction(mod, "recognize_face");
    WasmEdge_Value params[4];
    params[0] = WasmEdge_ValueGenI32(offset);
    params[1] = WasmEdge_ValueGenI32(len);
    params[2] = WasmEdge_ValueGenI32(width);
    params[3] = WasmEdge_ValueGenI32(height);
    WasmEdge_Value returns[1];
    WasmEdge_Result res = WasmEdge_FunctionCall(func, params, 4, returns, 1);

    jstring result;
    if (WasmEdge_ResultOK(res)) {
        const char* result_str = (const char*)WasmEdge_ValueGetWasmEdgeString(returns[0]).buf;
        result = env->NewStringUTF(result_str);
    } else {
        result = env->NewStringUTF("Error");
    }

    env->ReleaseByteArrayElements(imageData, bytes, 0);
    WasmEdge_VMDelete(vm);
    WasmEdge_ConfigureDelete(conf);
    return result;
}