package com.github.distanteye.ep_utils.core;
/**
 * Make sure anything in the DataFileProcessor has getName and getType
 * This allows further decisions to be made
 * @author Vigilant
 */
public interface UniqueNamedData {
	String getName();
	String getType(); // returns Table, Package, etc
}
