package predictions2;

import org.json.JSONObject;
import org.json.XML;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

@WebServlet("/predictions2/")
public class PredictionsServlet extends HttpServlet {
    private Predictions predictions; // backend bean

    // Executed when servlet is first loaded into container.
    // Create a Predictions object and set its servletContext
    // property so that the object can do I/O.
    @Override
    public void init() {
        predictions = new Predictions();
        predictions.setServletContext(this.getServletContext());
    }

    // GET /predictions2
    // GET /predictions2?id=1
    // If the HTTP Accept header is set to application/json (or an equivalent
    // such as text/x-json), the response is JSON and XML otherwise.
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        String param = request.getParameter("id");
        Integer key = (param == null) ? null : new Integer(param.trim());
        // Check user preference for XML or JSON by inspecting
        // the HTTP headers for the Accept key.
        boolean json = false;
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains("json")) json = true;
        // If no query string, assume client wants the full list.
        if (key == null) {
            ConcurrentMap<Integer, Prediction> map = predictions.getMap();
            // Sort the map's values for readability.
            Object[] list = map.values().toArray();
            Arrays.sort(list);
            String xml = predictions.toXML(list);
            sendResponse(response, xml, json);
        }
        // Otherwise, return the specified Prediction.
        else {
            Prediction pred = predictions.getMap().get(key);
            if (pred == null) { // no such Prediction
                String msg = key + " does not map to a prediction.\n";
                sendResponse(response, predictions.toXML(msg), false);
            } else { // requested Prediction found
                sendResponse(response, predictions.toXML(pred), json);
            }
        }
    }// POST /predictions2

    // HTTP body should contain two keys, one for the predictor ("who") and
    // another for the prediction ("what").
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String who = request.getParameter("who");
        String what = request.getParameter("what");
        // Are the data to create a new prediction present?
        if (who == null || what == null)
            throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
        // Create a Prediction.
        Prediction p = new Prediction();
        p.setWho(who);
        p.setWhat(what);
        // Save the ID of the newly created Prediction.
        int id = predictions.addPrediction(p);
        // Generate the confirmation message.
        String msg = "Prediction " + id + " created.\n";
        sendResponse(response, predictions.toXML(msg), false);
    }

    // PUT /predictions
    // HTTP body should contain at least two keys: the prediction's id
    // and either who or what.
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        /* A workaround is necessary for a PUT request because neither Tomcat
           nor Jetty generates a workable parameter map for this HTTP verb. */
        String key = null;
        String rest = null;
        boolean who = false;
        /* Let the hack begin. */
        try {
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(request.getInputStream()));
            String data = br.readLine();
            /* To simplify the hack, assume that the PUT request has exactly
               two parameters: the id and either who or what. Assume, further,
               that the id comes first. From the client side, a hash character
               # separates the id and the who/what, e.g.,
                  id=33#who=Homer Allision
            */
            String[] args = data.split("#");      // id in args[0], rest in args[1]
            String[] parts1 = args[0].split("="); // id = parts1[1]
            key = parts1[1];
            String[] parts2 = args[1].split("="); // parts2[0] is key
            if (parts2[0].contains("who")) who = true;
            rest = parts2[1];
        } catch (Exception e) {
            throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        // If no key, then the request is ill formed.
        if (key == null)
            throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
        // Look up the specified prediction.
        Prediction p = predictions.getMap().get(new Integer(key.trim()));
        if (p == null) { // not found?
            String msg = key + " does not map to a Prediction.\n";
            sendResponse(response, predictions.toXML(msg), false);
        } else { // found
            if (rest == null) {
                throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
            }
            // Do the editing.
            else {
                if (who) p.setWho(rest);
                else p.setWhat(rest);
                String msg = "Prediction " + key + " has been edited.\n";
                sendResponse(response, predictions.toXML(msg), false);
            }
        }
    }

    // DELETE /predictions2?id=1
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        String param = request.getParameter("id");
        Integer key = (param == null) ? null : new Integer(param.trim());
        // Only one Prediction can be deleted at a time.
        if (key == null)
            throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
        try {
            predictions.getMap().remove(key);
            String msg = "Prediction " + key + " removed.\n";
            sendResponse(response, predictions.toXML(msg), false);
        } catch (Exception e) {
            throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // Method Not Allowed
    @Override
    public void doTrace(HttpServletRequest request, HttpServletResponse response) {
        throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    public void doHead(HttpServletRequest request, HttpServletResponse response) {
        throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    public void doOptions(HttpServletRequest request, HttpServletResponse response) {
        throw new HTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
// Send the response payload to the client.

    private void sendResponse(HttpServletResponse response,
                              String payload,
                              boolean json) {
        try {
            // Convert to JSON?
            if (json) {
                JSONObject jobt = XML.toJSONObject(payload);
                payload = jobt.toString(3); // 3 is indentation level for nice look
            }
            OutputStream out = response.getOutputStream();
            out.write(payload.getBytes());
            out.flush();
        } catch (Exception e) {
            throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}