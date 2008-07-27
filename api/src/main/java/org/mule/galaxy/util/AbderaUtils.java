package org.mule.galaxy.util;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.context.SimpleResponseContext;
import org.mule.galaxy.policy.ApprovalMessage;
import org.mule.galaxy.policy.PolicyException;

public class AbderaUtils {

    public static ResponseContextException newErrorMessage(final String title,
                                   final String message, int status) throws ResponseContextException {
        SimpleResponseContext rc = new SimpleResponseContext() {

            @Override
            protected void writeEntity(Writer writer) throws IOException {
                writer.write("<html><head><title>)");
                writer.write(title);
                writer.write("</title></head><body><div class=\"error\">");
                writer.write(message);
                writer.write("</div></body></html>");
            }

            public boolean hasEntity() {
                return true;
            }
        };
        
        rc.setStatus(status);
        
        return new ResponseContextException(rc);
    }


    public static void assertNotEmpty(Object o, String message) throws ResponseContextException {
        if (o == null || "".equals(o)) {
            throwMalformed(message);
        }
    }

    public static void throwMalformed(final String message) throws ResponseContextException {
        throw newErrorMessage("Malformed Atom Entry", message, 400);
    }
    public static ResponseContextException createArtifactPolicyExceptionResponse(PolicyException e) {
        final StringBuilder s = new StringBuilder();
        s.append("<html><head><title>Artifact Policy Failure</title></head><body>");
        
        List<ApprovalMessage> approvals = e.getApprovals();
        
        for (ApprovalMessage m : approvals) {
            if (m.isWarning()) {
                s.append("<div class=\"warning\">");
            } else {
                s.append("<div class=\"failure\">");
            }
            
            s.append(m.getMessage());
            s.append("</div>");
        }
        
        s.append("</body></html>");
        SimpleResponseContext rc = new SimpleResponseContext() {
            @Override
            protected void writeEntity(Writer writer) throws IOException {
                writer.write(s.toString());
                writer.flush();
            }

            public boolean hasEntity() {
                return true;
            }
        };
        rc.setContentType("application/xhtml");
        // bad request code
        rc.setStatus(400);
        
        return new ResponseContextException(rc);
    }
}
