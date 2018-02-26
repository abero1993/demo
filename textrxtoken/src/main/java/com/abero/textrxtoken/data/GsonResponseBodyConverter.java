package com.abero.testrxtoken.data;

import android.util.Log;

import com.abero.testrxtoken.data.execption.*;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by Administrator on 2017/8/11.
 */

final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private static final String TAG = "GsonResponseBodyConvert";
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    GsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {

        try {
            HttpResult apiModel = (HttpResult) adapter.fromJson(value.charStream());
            Log.i(TAG, "convert: code=" + apiModel.getErrcode());
            if (apiModel.getErrcode() == 2) {
                throw new TokenNotExistException();
            } else if (apiModel.getErrcode() == 3) {
                throw new TokenInvalidException();
            } else {
                return (T) apiModel.getData();
            }

        } finally {
            value.close();
        }

    }
}
