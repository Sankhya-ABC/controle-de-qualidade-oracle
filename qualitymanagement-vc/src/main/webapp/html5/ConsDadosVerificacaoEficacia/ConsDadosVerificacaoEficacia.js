angular.module('ConsDadosVerificacaoEficaciaApp', ['snk'])
    .controller('ConsDadosVerificacaoEficaciaController', ['$scope', 'Criteria', 'MessageUtils', 'SkApplication', 'ObjectUtils',
        function ($scope, Criteria, MessageUtils, SkApplication, ObjectUtils) {

            let self = this;

            ObjectUtils.implements(self, IDynaformInterceptor);
            ObjectUtils.implements(self, IFormInterceptor);
            ObjectUtils.implements(self, IDatagridInterceptor);

            self.init = init;
            self.onDynaformLoad = onDynaformLoad;
            let criteria = new CriteriaProvider();
            criteria.getCriteria = getCriteria;

            self._dataset;
            self._dynaform;

            function init() {

            }

            function readPermissions() {
                var authData = SkApplication.instance().getAuthorizationData();

            }

            function onDynaformLoad(dynaform, dataset) {
                self._dataset = dataset;
                self._dynaform = dynaform;
                self._dataset.addCriteriaProvider(criteria);
            }

            function getCriteria() {
                let criteria = Criteria();
                return criteria
            }

        }])