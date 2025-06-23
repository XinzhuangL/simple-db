package org.lxz.server;

import org.lxz.sql.analyzer.Analyzer;

public class GlobalStateMgr {

    private static final Analyzer analyzer = new Analyzer(Analyzer.AnalyzerVisitor.getInstance());
    private static final MetadataMgr metadataMgr = new MetadataMgr(new LocalMetastore());

    public static Analyzer getAnalyzer() {
        return analyzer;
    }

    public static MetadataMgr getMetadataMgr() {
        return metadataMgr;
    }

}
