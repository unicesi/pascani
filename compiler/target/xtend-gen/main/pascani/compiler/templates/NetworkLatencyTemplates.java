/**
 * Copyright © 2015 Universidad Icesi
 * 
 * This file is part of the Pascani library.
 * 
 * The Pascani library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Pascani library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with The SLR Support Tools. If not, see <http://www.gnu.org/licenses/>.
 */
package pascani.compiler.templates;

import com.google.common.base.Joiner;
import java.util.Collection;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import pascani.compiler.util.FilenameProposal;
import pascani.lang.Event;
import pascani.lang.events.NetworkLatencyEvent;
import pascani.lang.infrastructure.rabbitmq.RabbitMQProducer;

@SuppressWarnings("all")
public class NetworkLatencyTemplates {
  /**
   * Generates the body for methods within the initial adapter (where the measurement starts, and the
   * return measurement ends) for a modified interface, measuring network latency data.
   */
  public static String initialAdapterMethod(final String startVar, final String eventVar, final List<String> eventParams, final String eVar, final boolean isVoid, final String referenceVar, final String producerVar, final String methodName, final String methodReturn, final Collection<String> paramNames) {
    String _xblockexpression = null;
    {
      String _xifexpression = null;
      int _size = paramNames.size();
      boolean _greaterThan = (_size > 0);
      if (_greaterThan) {
        Joiner _on = Joiner.on(", ");
        String _join = _on.join(paramNames);
        _xifexpression = (", " + _join);
      } else {
        _xifexpression = "";
      }
      String params = _xifexpression;
      String _xifexpression_1 = null;
      if ((!isVoid)) {
        FilenameProposal _filenameProposal = new FilenameProposal("_return", paramNames);
        _xifexpression_1 = _filenameProposal.getNewName();
      }
      final String _return = _xifexpression_1;
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("long ");
      _builder.append(startVar, "");
      _builder.append(" = System.nanoTime();");
      _builder.newLineIfNotEmpty();
      String _simpleName = NetworkLatencyEvent.class.getSimpleName();
      _builder.append(_simpleName, "");
      _builder.append(" ");
      _builder.append(eventVar, "");
      _builder.append(" = null;");
      _builder.newLineIfNotEmpty();
      _builder.append("try {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append(eventVar, "\t");
      _builder.append(" = new ");
      String _simpleName_1 = NetworkLatencyEvent.class.getSimpleName();
      _builder.append(_simpleName_1, "\t");
      _builder.append("(");
      Joiner _on_1 = Joiner.on(", ");
      String _join_1 = _on_1.join(eventParams);
      _builder.append(_join_1, "\t");
      _builder.append(");");
      _builder.newLineIfNotEmpty();
      _builder.append("} catch(Exception ");
      _builder.append(eVar, "");
      _builder.append(") { ");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("throw new RuntimeException(");
      _builder.append(eVar, "\t");
      _builder.append(");");
      _builder.newLineIfNotEmpty();
      _builder.append("}");
      _builder.newLine();
      {
        if (isVoid) {
          _builder.append("this.");
          _builder.append(referenceVar, "");
          _builder.append(".");
          _builder.append(methodName, "");
          _builder.append("(");
          _builder.append(eventVar, "");
          _builder.append(params, "");
          _builder.append(");");
          _builder.newLineIfNotEmpty();
        } else {
          String _simpleName_2 = NetworkLatencyEvent.class.getSimpleName();
          _builder.append(_simpleName_2, "");
          _builder.append(" ");
          _builder.append(_return, "");
          _builder.append(" = new ");
          String _simpleName_3 = NetworkLatencyEvent.class.getSimpleName();
          _builder.append(_simpleName_3, "");
          _builder.append("(");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("this.");
          _builder.append(referenceVar, "\t");
          _builder.append(".");
          _builder.append(methodName, "\t");
          _builder.append("(");
          _builder.append(eventVar, "\t");
          _builder.append(params, "\t");
          _builder.append("), ");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("System.nanoTime()");
          _builder.newLine();
          _builder.append(");");
          _builder.newLine();
          _builder.append(producerVar, "");
          _builder.append(".produce(");
          _builder.append(_return, "");
          _builder.append(");");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("return (");
          _builder.append(methodReturn, "");
          _builder.append(") ");
          _builder.append(_return, "");
          _builder.append(".getActualMethodReturn();");
          _builder.newLineIfNotEmpty();
        }
      }
      _xblockexpression = _builder.toString();
    }
    return _xblockexpression;
  }
  
  /**
   * Generates the body for methods within the final adapter (where the measurement ends, and the return
   * measurement starts) for a modified interface, measuring network latency data.
   */
  public static String finalAdapterMethod(final String endVar, final String eventVar, final String newEventVar, final boolean isVoid, final String referenceVar, final String producerVar, final String methodName, final Collection<String> paramNames) {
    String _xblockexpression = null;
    {
      String _xifexpression = null;
      if ((!isVoid)) {
        FilenameProposal _filenameProposal = new FilenameProposal("_return", paramNames);
        _xifexpression = _filenameProposal.getNewName();
      }
      final String _return = _xifexpression;
      String _xifexpression_1 = null;
      if ((!isVoid)) {
        FilenameProposal _filenameProposal_1 = new FilenameProposal("_returnEvent", paramNames);
        _xifexpression_1 = _filenameProposal_1.getNewName();
      }
      final String _returnEvent = _xifexpression_1;
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("long ");
      _builder.append(endVar, "");
      _builder.append(" = System.nanoTime();");
      _builder.newLineIfNotEmpty();
      String _simpleName = NetworkLatencyEvent.class.getSimpleName();
      _builder.append(_simpleName, "");
      _builder.append(" ");
      _builder.append(newEventVar, "");
      _builder.append(" = ");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("new ");
      String _simpleName_1 = NetworkLatencyEvent.class.getSimpleName();
      _builder.append(_simpleName_1, "\t");
      _builder.append(" (");
      _builder.append(eventVar, "\t");
      _builder.append(", ");
      _builder.append(endVar, "\t");
      _builder.append(");");
      _builder.newLineIfNotEmpty();
      _builder.append(producerVar, "");
      _builder.append(".produce(");
      _builder.append(newEventVar, "");
      _builder.append(");");
      _builder.newLineIfNotEmpty();
      _builder.newLine();
      {
        if (isVoid) {
          _builder.append("this.");
          _builder.append(referenceVar, "");
          _builder.append(".");
          _builder.append(methodName, "");
          _builder.append("(");
          Joiner _on = Joiner.on(", ");
          String _join = _on.join(paramNames);
          _builder.append(_join, "");
          _builder.append(");");
          _builder.newLineIfNotEmpty();
        } else {
          _builder.append("Object ");
          _builder.append(_return, "");
          _builder.append(" = this.");
          _builder.append(referenceVar, "");
          _builder.append(".");
          _builder.append(methodName, "");
          _builder.append("(");
          Joiner _on_1 = Joiner.on(", ");
          String _join_1 = _on_1.join(paramNames);
          _builder.append(_join_1, "");
          _builder.append(");");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          String _simpleName_2 = NetworkLatencyEvent.class.getSimpleName();
          _builder.append(_simpleName_2, "");
          _builder.append(" ");
          _builder.append(_returnEvent, "");
          _builder.append(" = new ");
          String _simpleName_3 = NetworkLatencyEvent.class.getSimpleName();
          _builder.append(_simpleName_3, "");
          _builder.append("(");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append(eventVar, "\t");
          _builder.append(".transactionId(),");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("System.nanoTime(),");
          _builder.newLine();
          _builder.append("\t");
          _builder.append(eventVar, "\t");
          _builder.append(".methodCaller(),");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append(eventVar, "\t");
          _builder.append(".methodProvider(),");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append(_return, "\t");
          _builder.append(",");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append(eventVar, "\t");
          _builder.append(".getMethodInformation(),");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append(eventVar, "\t");
          _builder.append(".getActualMethodParameters()");
          _builder.newLineIfNotEmpty();
          _builder.append(");");
          _builder.newLine();
          _builder.newLine();
          _builder.append("return ");
          _builder.append(_returnEvent, "");
          _builder.append(";");
          _builder.newLineIfNotEmpty();
        }
      }
      _xblockexpression = _builder.toString();
    }
    return _xblockexpression;
  }
  
  /**
   * Simple template for getting a method based on the name and the parameters types
   */
  public static String getMethod(final String name, final Collection<String> paramTypes) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("this.getClass().getMethod(\"");
    _builder.append(name, "");
    _builder.append("\", ");
    Joiner _on = Joiner.on(", ");
    String _join = _on.join(paramTypes);
    _builder.append(_join, "");
    _builder.append(")");
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }
  
  /**
   * Produces a code block for initializing the message producer inside each adapter
   */
  public static String getProducerInitialization(final String producerVar, final String host, final int port, final String virtualHost, final String exchange, final String routingKey, final boolean durableExchange) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("try {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("EndPoint endPoint = new EndPoint.Builder(\"");
    _builder.append(host, "\t");
    _builder.append("\", ");
    _builder.append(port, "\t");
    _builder.append(", \"");
    _builder.append(virtualHost, "\t");
    _builder.append("\").build();");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("List<Class<? extends ");
    String _simpleName = Event.class.getSimpleName();
    _builder.append(_simpleName, "\t");
    _builder.append("<?>>> classes = ");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("new ArrayList<Class<? extends ");
    String _simpleName_1 = Event.class.getSimpleName();
    _builder.append(_simpleName_1, "\t\t");
    _builder.append("<?>>>();");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("classes.add(");
    String _simpleName_2 = NetworkLatencyEvent.class.getSimpleName();
    _builder.append(_simpleName_2, "\t");
    _builder.append(".class);");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("this.");
    _builder.append(producerVar, "\t");
    _builder.append(" = new ");
    String _simpleName_3 = RabbitMQProducer.class.getSimpleName();
    _builder.append(_simpleName_3, "\t");
    _builder.append("(endPoint, classes, ");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("\"");
    _builder.append(exchange, "\t\t");
    _builder.append("\", \"");
    _builder.append(routingKey, "\t\t");
    _builder.append("\", ");
    _builder.append(durableExchange, "\t\t");
    _builder.append(");");
    _builder.newLineIfNotEmpty();
    _builder.append("} catch (Exception e) {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("throw new RuntimeException(e);");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    return _builder.toString();
  }
}
