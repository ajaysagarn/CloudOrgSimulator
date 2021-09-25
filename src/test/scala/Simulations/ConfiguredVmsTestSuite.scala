package Simulations

import CloudSimWrappers.{ConfiguredBroker, ConfiguredDataCenter, ConfiguredVm}
import HelperUtils.CloudSimUtils
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Test suite to test the successful creation of vms as per a given configuration
 */
class ConfiguredVmsTestSuite extends AnyFlatSpec with Matchers{
  val config = CloudSimUtils.getconfigValue("Saas","saasDataCenter")
  val testSim = new CloudSim()

  behavior of "Configured Datacenter module"

  "ConfiguredDatacenter" should "Create datacenter with correct costs" in {
    val broker = new ConfiguredBroker(testSim,config).getConfiguredBroker()
    val vms = new ConfiguredVm(testSim,broker,config).getVms

    assert(vms.size == config.getInt("vm.InitialCount"))

    vms.foreach(vm =>{
      assert(vm.getRam.getCapacity == config.getLong("vm.RAMInMBs")) //verify ram is same as in config
      assert(vm.getBw.getCapacity == config.getLong("vm.BandwidthInMBps")) //verify bandwidth is same as in config
      assert(vm.getStorage.getCapacity == config.getLong("vm.StorageInMMapBs")) //verify storage is same as in config
      assert(vm.getSubmissionDelay == config.getDouble("vm.DefaultSubmissionDelay")) //verify submissino delay is same as in config
    })

  }
}
