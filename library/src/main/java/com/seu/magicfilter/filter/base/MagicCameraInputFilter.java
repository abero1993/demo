package com.seu.magicfilter.filter.base;

import com.seu.magicfilter.R;
import com.seu.magicfilter.filter.base.gpuimage.GPUImageFilter;

/**
 * 绘制纹理到屏幕
 *
 * @author Created by jz on 2017/5/2 17:53
 */
public class MagicCameraInputFilter extends GPUImageFilter {

    public MagicCameraInputFilter() {
        super(R.raw.default_vertex, R.raw.default_fragment);
    }

}