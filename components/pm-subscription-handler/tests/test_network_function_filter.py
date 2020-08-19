from unittest import TestCase
from parameterized import parameterized
from mod.network_function import NetworkFunctionFilter


def custom_name_func(testcase_func, param_num, param):
    return "%s_%s" % (
        testcase_func.__name__,
        parameterized.to_safe_name((str(param.args[5]))),
    )


class NetworkFunctionFilterTest(TestCase):

    @parameterized.expand([
        ("^pnf.*", "7129e420-d396-4efb-af02-6b83499b12f8", "e80a6ae3-cafd-4d24-850d-e14c084a5ca9", "Active", True, "test_filter_true_on_xnf"),
        ("^PNF.*", "7129e420-d396-4efb-af02-6b83499b12f8", "e80a6ae3-cafd-4d24-850d-e14c084a5ca9", "Active", False, "test_filter_false_on_xnf"),
        ("^pnf.*", "WrongInvariantUUID", "e80a6ae3-cafd-4d24-850d-e14c084a5ca9", "Active", False, "test_filter_false_on_modelInvariantUUIDs"),
        ("^pnf.*", "7129e420-d396-4efb-af02-6b83499b12f8", "WrongUUID", "Active", False, "test_filter_false_on_modelVersionIDs"),
        ("^pnf.*", "7129e420-d396-4efb-af02-6b83499b12f8", "e80a6ae3-cafd-4d24-850d-e14c084a5ca9", "Inventoried", False, "test_filter_false_on_OrchestrationStatus"),
        ], name_func=custom_name_func)
    def testRunner(self, a, b, c, d, e, f):
        nf_filter = NetworkFunctionFilter(**{
            "nfNames": [
                a
            ],
            "modelInvariantUUIDs": [
                b
            ],
            "modelVersionIDs": [
                c
            ]
        })
        # print("Performing test " + filter["testName"])
        self.assertEqual(nf_filter.is_nf_in_filter("pnf-11",
                                                   "7129e420-d396-4efb-af02-6b83499b12f8",
                                                   "e80a6ae3-cafd-4d24-850d-e14c084a5ca9",
                                                   d), e)

    def test_filter_true_on_multiple_modelInvariantUUIDs(self):
        nf_filter = NetworkFunctionFilter(**{
            "nfNames": [
            ],
            "modelInvariantUUIDs": [
                '5845y423-g654-6fju-po78-8n53154532k6',
                '7129e420-d396-4efb-af02-6b83499b12f8'
            ],
            "modelVersionIDs": [
            ]
        })
        self.assertTrue(nf_filter.is_nf_in_filter('pnf1',
                                                  '7129e420-d396-4efb-af02-6b83499b12f8',
                                                  'e80a6ae3-cafd-4d24-850d-e14c084a5ca9',
                                                  'Active'))

    def test_filter_false_on_modelInvariantUUIDs_being_false_and_pnfname_being_true(self):
        nf_filter = NetworkFunctionFilter(**{
            "nfNames": [
                "^pnf.*",
                "^vnf.*"
            ],
            "modelInvariantUUIDs": [
                '5845y423-g654-6fju-po78-8n53154532k6',
                '7129e420-d396-4efb-af02-6b83499b12f8'
            ],
            "modelVersionIDs": [
            ]
        })
        self.assertFalse(nf_filter.is_nf_in_filter('pnf1',
                                                   'WrongModelInvariantUUID',
                                                   'e80a6ae3-cafd-4d24-850d-e14c084a5ca9',
                                                   'Active'))
