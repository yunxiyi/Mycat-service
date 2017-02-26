/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese
 * opensource volunteers. you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 *
 */
package io.mycat.config.model;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import io.mycat.config.model.rule.RuleConfig;
import io.mycat.util.SplitUtil;

/**
 * @author mycat
 */
public class TableConfig {
	public static int TYPE_GLOBAL_TABLE = 1;
	public static int TYPE_GLOBAL_DEFAULT = 0;
	private String name;
	private String primaryKey;
	private boolean autoIncrement;
	private boolean needAddLimit;
	private Set<String> dbTypes;
	private int tableType;
	private ArrayList<String> dataNodes;
	private ArrayList<String> distTables;
	private RuleConfig rule;
	private String partitionColumn;
	private boolean isRuleRequired;
	private TableConfig parentTC;
	private boolean childTable;
	private String joinKey;
	private String parentKey;
	private String locateRTableKeySql;
	// only has one level of parent
	private boolean isSecondLevel;
	private boolean isPartionKeyIsPrimaryKey;
	private final Random rand = new Random();

	private volatile List<SQLTableElement> tableElementList;
	private volatile String tableStructureSQL;
	private volatile Map<String,List<String>> dataNodeTableStructureSQLMap;
	private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock(false);

	public TableConfig(){

	}

	public TableConfig(String name, String primaryKey, boolean autoIncrement,boolean needAddLimit, int tableType,
			String dataNode,Set<String> dbType, RuleConfig rule, boolean ruleRequired,
			TableConfig parentTC, boolean isChildTable, String joinKey,
			String parentKey,String subTables) {
		if (name == null) {
			throw new IllegalArgumentException("table name is null");
		} else if (dataNode == null) {
			throw new IllegalArgumentException("dataNode name is null");
		}
		this.primaryKey = primaryKey;
		this.autoIncrement = autoIncrement;
		this.needAddLimit=needAddLimit;
		this.tableType = tableType;
		this.dbTypes=dbType;
		if (ruleRequired && rule == null) {
			throw new IllegalArgumentException("ruleRequired but rule is null");
		}

		this.name = name.toUpperCase();
		
		String theDataNodes[] = SplitUtil.split(dataNode, ',', '$', '-');
		if (theDataNodes == null || theDataNodes.length <= 0) {
			throw new IllegalArgumentException("invalid table dataNodes: " + dataNode);
		}
		dataNodes = new ArrayList<String>(theDataNodes.length);
		for (String dn : theDataNodes) {
			dataNodes.add(dn);
		}
		
		if(subTables!=null && !subTables.equals("")){
			String sTables[] = SplitUtil.split(subTables, ',', '$', '-');
			if (sTables == null || sTables.length <= 0) {
				throw new IllegalArgumentException("invalid table subTables");
			}
			this.distTables = new ArrayList<String>(sTables.length);
			for (String table : sTables) {
				distTables.add(table);
			}
		}else{
			this.distTables = new ArrayList<String>();
		}	
		
		this.rule = rule;
		this.partitionColumn = (rule == null) ? null : rule.getColumn();
		isPartionKeyIsPrimaryKey=(partitionColumn==null)?primaryKey==null:partitionColumn.equals(primaryKey);
		this.isRuleRequired = ruleRequired;
		this.childTable = isChildTable;
		this.parentTC = parentTC;
		this.joinKey = joinKey;
		this.parentKey = parentKey;
		if (parentTC != null) {
			locateRTableKeySql = genLocateRootParentSQL();
			isSecondLevel = (parentTC.parentTC == null);
		} else {
			locateRTableKeySql = null;
			isSecondLevel = false;
		}
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

    public Set<String> getDbTypes()
    {
        return dbTypes;
    }

    public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public boolean isNeedAddLimit() {
		return needAddLimit;
	}

	public boolean getIsSecondLevel() {
		return isSecondLevel;
	}

	public String getLocateRTableKeySql() {
		return locateRTableKeySql;
	}

	public boolean isGlobalTable() {
		return this.tableType == this.TYPE_GLOBAL_TABLE;
	}

	public String genLocateRootParentSQL() {
		TableConfig tb = this;
		StringBuilder tableSb = new StringBuilder();
		StringBuilder condition = new StringBuilder();
		TableConfig prevTC = null;
		int level = 0;
		String latestCond = null;
		while (tb.parentTC != null) {
			tableSb.append(tb.parentTC.name).append(',');
			String relation = null;
			if (level == 0) {
				latestCond = " " + tb.parentTC.getName() + '.' + tb.parentKey
						+ "=";
			} else {
				relation = tb.parentTC.getName() + '.' + tb.parentKey + '='
						+ tb.name + '.' + tb.joinKey;
				condition.append(relation).append(" AND ");
			}
			level++;
			prevTC = tb;
			tb = tb.parentTC;
		}
		String sql = "SELECT "
				+ prevTC.parentTC.name
				+ '.'
				+ prevTC.parentKey
				+ " FROM "
				+ tableSb.substring(0, tableSb.length() - 1)
				+ " WHERE "
				+ ((level < 2) ? latestCond : condition.toString() + latestCond);
		// System.out.println(this.name+" sql " + sql);
		return sql;

	}

	public String getPartitionColumn() {
		return partitionColumn;
	}

	public int getTableType() {
		return tableType;
	}

	/**
	 * get root parent
	 *
	 * @return
	 */
	public TableConfig getRootParent() {
		if (parentTC == null) {
			return null;
		}
		TableConfig preParent = parentTC;
		TableConfig parent = preParent.getParentTC();

		while (parent != null) {
			preParent = parent;
			parent = parent.getParentTC();
		}
		return preParent;
	}

	public String getRandomDataNode() {
		int index = Math.abs(rand.nextInt(Integer.MAX_VALUE)) % dataNodes.size();
		return dataNodes.get(index);
	}

	public boolean isDistTable(){
		if(this.distTables!=null && !this.distTables.isEmpty() ){
			return true;
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public void setNeedAddLimit(boolean needAddLimit) {
		this.needAddLimit = needAddLimit;
	}

	public void setDbTypes(Set<String> dbTypes) {
		this.dbTypes = dbTypes;
	}

	public void setTableType(int tableType) {
		this.tableType = tableType;
	}

	public ArrayList<String> getDataNodes() {
		return dataNodes;
	}

	public void setDataNodes(ArrayList<String> dataNodes) {
		this.dataNodes = dataNodes;
	}

	public ArrayList<String> getDistTables() {
		return distTables;
	}

	public void setDistTables(ArrayList<String> distTables) {
		this.distTables = distTables;
	}

	public RuleConfig getRule() {
		return rule;
	}

	public void setRule(RuleConfig rule) {
		this.rule = rule;
	}

	public void setPartitionColumn(String partitionColumn) {
		this.partitionColumn = partitionColumn;
	}

	public TableConfig getParentTC() {
		return parentTC;
	}

	public void setParentTC(TableConfig parentTC) {
		this.parentTC = parentTC;
	}

	public boolean isChildTable() {
		return childTable;
	}

	public void setChildTable(boolean childTable) {
		this.childTable = childTable;
	}

	public String getJoinKey() {
		return joinKey;
	}

	public void setJoinKey(String joinKey) {
		this.joinKey = joinKey;
	}

	public String getParentKey() {
		return parentKey;
	}

	public void setParentKey(String parentKey) {
		this.parentKey = parentKey;
	}

	public void setLocateRTableKeySql(String locateRTableKeySql) {
		this.locateRTableKeySql = locateRTableKeySql;
	}

	public void setIsSecondLevel(boolean isSecondLevel) {
		this.isSecondLevel = isSecondLevel;
	}

	public boolean getIsPartionKeyIsPrimaryKey() {
		return isPartionKeyIsPrimaryKey;
	}

	public void setIsPartionKeyIsPrimaryKey(boolean isPartionKeyIsPrimaryKey) {
		this.isPartionKeyIsPrimaryKey = isPartionKeyIsPrimaryKey;
	}

	public Random getRand() {
		return rand;
	}

	public List<SQLTableElement> getTableElementList() {
		return tableElementList;
	}

	public void setTableElementList(List<SQLTableElement> tableElementList) {
		this.tableElementList = tableElementList;
	}

	public String getTableStructureSQL() {
		return tableStructureSQL;
	}

	public void setTableStructureSQL(String tableStructureSQL) {
		this.tableStructureSQL = tableStructureSQL;
	}

	public Map<String, List<String>> getDataNodeTableStructureSQLMap() {
		return dataNodeTableStructureSQLMap;
	}

	public void setDataNodeTableStructureSQLMap(Map<String, List<String>> dataNodeTableStructureSQLMap) {
		this.dataNodeTableStructureSQLMap = dataNodeTableStructureSQLMap;
	}

	public ReentrantReadWriteLock getReentrantReadWriteLock() {
		return reentrantReadWriteLock;
	}

	public void setReentrantReadWriteLock(ReentrantReadWriteLock reentrantReadWriteLock) {
		this.reentrantReadWriteLock = reentrantReadWriteLock;
	}


	public boolean getIsRuleRequired() {
		return isRuleRequired;
	}

	public void setIsRuleRequired(boolean isRuleRequired) {
		this.isRuleRequired = isRuleRequired;
	}
}