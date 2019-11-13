package quix.python

import quix.api.execute.ActiveQuery
import quix.api.users.User
import quix.core.results.SingleBuilder

object TryPython {

  import monix.execution.Scheduler.Implicits.global

  def main(args: Array[String]): Unit = {
    val executor = new PythonExecutor()
    val query = ActiveQuery("id", Seq(importQuix), User("default"))
    val builder = new SingleBuilder[PythonCode]
    val task = executor.runTask(query, builder)

    task.runSyncUnsafe()

    for (row <- builder.build())
      println(row)

    for (log <- builder.logs)
      println(log)
  }

  def samplePyBridge = PythonCode {
    """
      |from py4j.java_gateway import JavaGateway
      |from py4j.java_gateway import GatewayParameters
      |gateway = JavaGateway(gateway_parameters=GatewayParameters(auto_convert=True))
      |bridge = gateway.entry_point
      |bridge.fields(["abc", "def"])
      |bridge.row(["abc", 123])
      |""".stripMargin
  }

  def pyBridgeClass = PythonCode{
    """
      |class Bridge:
      |
      |    def __init__(self):
      |        import sys
      |        from py4j.java_gateway import JavaGateway
      |        from py4j.java_gateway import GatewayParameters
      |        self.gateway = JavaGateway(gateway_parameters=GatewayParameters(auto_convert=True, port=int(sys.argv[1])))
      |        self.bridge = self.gateway.entry_point
      |
      |    def fields(self, fields):
      |        self.bridge.fields(fields)
      |
      |    def row(self, row):
      |        self.bridge.row(row)
      |
      |
      |quix = Bridge()
      |
      |quix.fields(['abc', 'def'])
      |quix.row([123, 456])
      |
      |""".stripMargin
  }

  def importQuix = PythonCode{
    """
      |from quix import Quix
      |
      |quix = Quix()
      |
      |quix.fields(['abc', 'def'])
      |quix.row([123, 456])
      |
      |quix.error('error!!!!')
      |quix.info('message bla bla bla')
      |
      |""".stripMargin
  }
}
