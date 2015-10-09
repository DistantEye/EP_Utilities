/**
 * 
 */
package com.github.distanteye.ep_utils.simple_ext;

/**
 * Special annotation to denote the use of a FactoryMethod as the default constructor instead of the norm.
 * Only custom implementations will know to actually parse this, though.
 * 
 * @author Vigilant
 */
public interface FactoryMethod {
	String className();
	String methodName();
	String[] methodParams();
}
