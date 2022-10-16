/*
 * Copyright (c) Kseno 2021.
 */

package com.roguera.view;

public interface IViewBlock {

	IViewBlock[] empty = new IViewBlock[]{};

	void Init();

	void Draw();

	void Reset();

}
