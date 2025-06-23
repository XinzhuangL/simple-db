package org.lxz.sql.optimizer;

public class ScanOptimizeOption {
    private boolean canUseAnyColumn;
    private boolean canUseMinMaxCountOpt;
    private boolean usePartitionColumnValueOnly;

    public boolean isCanUseAnyColumn() {
        return canUseAnyColumn;
    }

    public void setCanUseAnyColumn(boolean canUseAnyColumn) {
        this.canUseAnyColumn = canUseAnyColumn;
    }

    public boolean isCanUseMinMaxCountOpt() {
        return canUseMinMaxCountOpt;
    }

    public void setCanUseMinMaxCountOpt(boolean canUseMinMaxCountOpt) {
        this.canUseMinMaxCountOpt = canUseMinMaxCountOpt;
    }

    public boolean isUsePartitionColumnValueOnly() {
        return usePartitionColumnValueOnly;
    }

    public void setUsePartitionColumnValueOnly(boolean usePartitionColumnValueOnly) {
        this.usePartitionColumnValueOnly = usePartitionColumnValueOnly;
    }
}
