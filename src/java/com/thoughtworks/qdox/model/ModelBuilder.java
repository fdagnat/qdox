package com.thoughtworks.qdox.model;

import com.thoughtworks.qdox.parser.Builder;
import com.thoughtworks.qdox.parser.structs.ClassDef;
import com.thoughtworks.qdox.parser.structs.MethodDef;
import com.thoughtworks.qdox.parser.structs.FieldDef;

import java.util.*;

public class ModelBuilder implements Builder {

	private List classes = new ArrayList();
	private JavaClass currentClass;
	private String lastComment;
	private List lastTagSet;
	private String packge;
	private List imports = new ArrayList();
	private ClassLibrary classLibrary = new ClassLibrary();
	
	public ModelBuilder(){}

	public ModelBuilder(ClassLibrary classLibrary) {
		this.classLibrary = classLibrary;
	}

	public void addPackage(String packageName) {
		this.packge = packageName;
	}

	public void addImport(String importName) {
		imports.add(importName);
	}

	public void addJavaDoc(String text) {
		lastComment = text;
		lastTagSet = new LinkedList();
	}

	public void addJavaDocTag(String tag, String text) {
		lastTagSet.add(new DocletTag(tag, text));
	}

	public void addClass(ClassDef def) {
		currentClass = new JavaClass();

		// basic details
		currentClass.setName(def.name);
		currentClass.setInterface(def.isInterface);

		// superclass
		if (def.isInterface) {
			currentClass.setSuperClass(null);
		}
		else {
			currentClass.setSuperClass(def.extendz.size() > 0 ? createType((String)def.extendz.toArray()[0]) : null);
		}

		// implements
		{
			Set implementSet = def.isInterface ? def.extendz : def.implementz;
			String[] implementz = new String[implementSet.size()];
			implementSet.toArray(implementz);
			currentClass.setImplementz(implementz);
		}

		// modifiers
		{
			String[] modifiers = new String[def.modifiers.size()];
			def.modifiers.toArray(modifiers);
			currentClass.setModifiers(modifiers);
		}

		// javadoc
		addJavaDoc(currentClass);
		classLibrary.add(packge + "." + currentClass.getName());
		classes.add(currentClass);
	}

	private Type createType(String typeName) {
		Type superclass = new Type(imports, typeName, classLibrary, packge);
		return superclass;
	}

	private void addJavaDoc(AbstractJavaEntity entity) {
		if (lastComment != null) {
			entity.setComment(lastComment);
			entity.setTags(lastTagSet);
		}
		lastComment = null;
	}

	public void addMethod(MethodDef def) {
		JavaMethod currentMethod = new JavaMethod();

		// basic details
		currentMethod.setName(def.name);
		currentMethod.setReturns(createType(def.returns));
		currentMethod.setConstructor(def.constructor);
		currentMethod.setDimensions(def.dimensions);

		// parameters
		{
			JavaParameter[] params = new JavaParameter[def.params.size()];
			int i = 0;
			for (Iterator iterator = def.params.iterator(); iterator.hasNext();) {
				FieldDef fieldDef = (FieldDef)iterator.next();
				params[i++] = new JavaParameter(createType(fieldDef.type), fieldDef.name, fieldDef.dimensions);
			}
			currentMethod.setParameters(params);
		}

		// exceptions
		{
			Type[] exceptions = new Type[def.exceptions.size()];
			int index=0;
			for (Iterator iter = def.exceptions.iterator(); iter.hasNext(); ){
				exceptions[index++] = createType((String)iter.next());
			}
			currentMethod.setExceptions(exceptions);
		}

		// modifiers
		{
			String[] modifiers = new String[def.modifiers.size()];
			def.modifiers.toArray(modifiers);
			currentMethod.setModifiers(modifiers);
		}

		// javadoc
		addJavaDoc(currentMethod);

		currentClass.addMethod(currentMethod);
	}

	public void addField(FieldDef def) {
		JavaField currentField = new JavaField();
		currentField.setName(def.name);
		currentField.setType(createType(def.type));
		currentField.setDimensions(def.dimensions);

		// modifiers
		{
			String[] modifiers = new String[def.modifiers.size()];
			def.modifiers.toArray(modifiers);
			currentField.setModifiers(modifiers);
		}

		// javadoc
		addJavaDoc(currentField);

		currentClass.addField(currentField);
	}

	public JavaSource getSource() {
		JavaSource result = new JavaSource();
		result.setPackge(packge);
		JavaClass[] clsArray = new JavaClass[classes.size()];
		classes.toArray(clsArray);
		result.setClasses(clsArray);
		String[] impArray = new String[imports.size()];
		imports.toArray(impArray);
		result.setImports(impArray);
		return result;
	}
}