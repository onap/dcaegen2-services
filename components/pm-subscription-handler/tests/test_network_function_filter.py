from unittest import TestCase

from mod.network_function import NetworkFunctionFilter


class NetworkFunctionFilterTest(TestCase):
    def test_filter_false_on_xnf(self):
        nf_filter = NetworkFunctionFilter(**{
            "nfNames": [
                "^pnf.*",
                "^vnf.*"
            ],
            "modelInvariantUUIDs": [
            ],
            "modelVersionIDs": [
            ]
        })
        self.assertFalse(nf_filter.is_nf_in_filter('PNF-33',
                                                   '7129e420-d396-4efb-af02-6b83499b12f8',
                                                   'e80a6ae3-cafd-4d24-850d-e14c084a5ca9',
                                                   'Active'))

    def test_filter_true_on_xnf(self):
        nf_filter = NetworkFunctionFilter(**{
            "nfNames": [
                "^pnf.*",
                "^vnf.*"
            ],
            "modelInvariantUUIDs": [
            ],
            "modelVersionIDs": [
            ]
        })
        self.assertTrue(nf_filter.is_nf_in_filter('pnf1',
                                                  '7129e420-d396-4efb-af02-6b83499b12f8',
                                                  'e80a6ae3-cafd-4d24-850d-e14c084a5ca9',
                                                  'Active'))

    def test_filter_true_on_modelInvariantUUIDs(self):
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

    def test_filter_false_on_modelInvariantUUIDs(self):
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
        self.assertFalse(nf_filter.is_nf_in_filter('pnf1',
                                                   'WrongModelInvariantUUID',
                                                   'e80a6ae3-cafd-4d24-850d-e14c084a5ca9',
                                                   'Active'))

    def test_filter_true_on_modelVersionIDs(self):
        nf_filter = NetworkFunctionFilter(**{
            "nfNames": [
            ],
            "modelInvariantUUIDs": [
            ],
            "modelVersionIDs": [
                'e80a6ae3-cafd-4d24-850d-e14c084a5ca9'
            ]
        })
        self.assertTrue(nf_filter.is_nf_in_filter('pnf1',
                                                  '7129e420-d396-4efb-af02-6b83499b12f8',
                                                  'e80a6ae3-cafd-4d24-850d-e14c084a5ca9',
                                                  'Active'))

    def test_filter_false_on_modelVersionIDs(self):
        nf_filter = NetworkFunctionFilter(**{
            "nfNames": [
            ],
            "modelInvariantUUIDs": [
            ],
            "modelVersionIDs": [
                'e80a6ae3-cafd-4d24-850d-e14c084a5ca9'
            ]
        })
        self.assertFalse(nf_filter.is_nf_in_filter('pnf1',
                                                   '7129e420-d396-4efb-af02-6b83499b12f8',
                                                   'WrongModelVersionID',
                                                   'Active'))

    def test_filter_false_on_OrchestrationStatus(self):
        nf_filter = NetworkFunctionFilter(**{
            "nfNames": [
                "^pnf.*",
                "^vnf.*"
            ],
            "modelInvariantUUIDs": [
            ],
            "modelVersionIDs": [
            ]
        })
        self.assertFalse(nf_filter.is_nf_in_filter('pnf1',
                                                   '7129e420-d396-4efb-af02-6b83499b12f8',
                                                   'WrongModelVersionID',
                                                   'Inventoried'))
