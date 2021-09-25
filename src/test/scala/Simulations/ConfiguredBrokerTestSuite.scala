package Simulations

import HelperUtils.CloudSimUtils
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import CloudSimWrappers.{ConfiguredBroker, ConfiguredDataCenter, ConfiguredVm}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.network.NetworkHost

/**
 * test suite to test the creation of broker as per a given configuration
 */
class ConfiguredBrokerTestSuite  extends AnyFlatSpec with Matchers {
  val Config = CloudSimUtils.getconfigValue("Saas","saasDataCenter")

  val testSim = new CloudSim()

  behavior of "Configured Broker module"

  "ConfiguredBroker" should "be instantiated successfully" in {
    val broker = new ConfiguredBroker(testSim,Config)
    assertCompiles("broker.getConfiguredBroker()")
  }

}
