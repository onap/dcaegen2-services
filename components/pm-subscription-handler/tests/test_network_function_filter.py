import json
import os
from unittest import TestCase

from mod.network_function import NetworkFunctionFilter




class NetworkFunctionFilterTest(TestCase):

    def setUp(self):
        with open(os.path.join(os.path.dirname(__file__), 'data/filter_test_data.json'), 'r') as data:
            self.filter_data = json.load(data)

    def test_filter(self):
        for filter in self.filter_data["data"]:
            nf_filter = NetworkFunctionFilter(**filter["nfFilter"])
            print("Performing test " + filter["testName"])
            self.assertEqual(nf_filter.is_nf_in_filter(filter["nfName"],
                                                      filter["modelInvariantUUID"],
                                                      filter["modelVersionID"],
                                                      filter["orchestration_status"]), filter["expectedResult"])



