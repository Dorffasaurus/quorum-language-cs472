/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quorum.language;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import quorum.Libraries.Language.Compile.CompilerResult_;
import quorum.Libraries.Language.Compile.Hints.Hint_;

/**
 *
 * @author stefika
 */
public class QuorumParserResult extends ParserResult {
    private boolean valid = true;
    private QuorumParser parser;
    CompilerResult_ recentResult = null;
    
    QuorumParserResult(Snapshot snapshot, QuorumParser p) {
        super(snapshot);
        parser = p;
    }
    
    @Override
    public List<? extends Error> getDiagnostics() {
        return parser.getFileErrors();
    }
    
    public ArrayList<Hint_> getHints() {
        return parser.getFileHints();
    }
    
    public CompilerResult_ getRecentResult() {
        return recentResult;
    }
    
    public void SetRecentResult(CompilerResult_ result) {
        recentResult = result;
    }
    
    @Override
    protected void invalidate() {
        valid = false;
    }
}
