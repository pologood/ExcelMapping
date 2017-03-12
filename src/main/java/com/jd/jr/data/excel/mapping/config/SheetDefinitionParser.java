package com.jd.jr.data.excel.mapping.config;

import com.jd.jr.data.excel.mapping.annotation.ExcelField;
import com.jd.jr.data.excel.mapping.annotation.ExcelSheet;
import com.jd.jr.data.excel.mapping.definition.FieldDefinition;
import com.jd.jr.data.excel.mapping.definition.SheetDefinition;
import com.jd.jr.data.excel.mapping.exceptions.DefinitionException;
import com.jd.jr.data.excel.mapping.utils.JAXBUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * *****************************************
 *
 * @author 邓超【dengchao5@jd.com】
 * @date 2017/3/9 21:28
 * *****************************************
 */
public class SheetDefinitionParser {


    public SheetDefinition parse(File file){
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new DefinitionException(e.getMessage(),e);
        }
        return parse(inputStream);
    }
    public SheetDefinition parse(InputStream inputStream){
        SheetDefinition sheetDefinition = null;
        try {
            sheetDefinition = JAXBUtils.fromXML(inputStream,SheetDefinition.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DefinitionException(e.getMessage(),e);
        }
        if(sheetDefinition == null){
            throw new DefinitionException("XML定义错误，不能从XML中获取有效配置！");
        }
        if(sheetDefinition.getClass() == null){
            throw new DefinitionException("XML定义错误，<sheet>节点中class定义不正确！");
        }
        if(sheetDefinition.getFieldDefinitions() == null || sheetDefinition.getFieldDefinitions().size()==0){
            throw new DefinitionException("XML定义错误，不能从XML中获取有效的字段定义配置！");
        }
        return sheetDefinition;
    }
    public SheetDefinition parse(Class<?> clazz){

        SheetDefinition sheetDefinition = new SheetDefinition();
        sheetDefinition.setClazz(clazz);

        ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);
        if(excelSheet != null){
            sheetDefinition.setName(excelSheet.name());
            sheetDefinition.setEnableStyle(excelSheet.enableStyle());
            sheetDefinition.setVersion(excelSheet.version());
            sheetDefinition.setDefaultColumnWidth(excelSheet.defaultColumnWidth());
        }
        Field[] fields = clazz.getDeclaredFields();
        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();
        FieldDefinition fieldDefinition;
        int i=0;
        for(Field field:fields){
            if(field.isAnnotationPresent(ExcelField.class)) {
                fieldDefinition = new FieldDefinition();
                fieldDefinition.setName(field.getName());

                ExcelField excelField = field.getAnnotation(ExcelField.class);
                fieldDefinition.setTitle(excelField.title());
                fieldDefinition.setWidth(excelField.width());
                fieldDefinition.setAlign(excelField.align());
                fieldDefinition.setFormat(excelField.format());
                fieldDefinition.setFormatter(excelField.formatter());
                if(excelField.order() == 0){
                    fieldDefinition.setOrder(i);
                }else {
                    fieldDefinition.setOrder(excelField.order());
                }
                fieldDefinition.setLocked(excelField.isLocked());
                fieldDefinitions.add(fieldDefinition);
            }
            i++;
        }
        sheetDefinition.setFieldDefinitions(fieldDefinitions);

        if(sheetDefinition.getFieldDefinitions() == null || sheetDefinition.getFieldDefinitions().size()==0){
            throw new DefinitionException("注解配置错误，不能从该类中获取有效的字段定义！");
        }
        return sheetDefinition;
    }
}