(ns integration.definition-test
  (:require
   [clojure.test :refer [deftest testing]]
   [integration.fixture :as fixture]
   [integration.helper :as h]
   [integration.lsp :as lsp]))

(lsp/clean-after-test)

(deftest definition
  (lsp/start-process!)
  (lsp/request! (fixture/initialize-request))
  (lsp/notify! (fixture/initialized-notification))
  (lsp/notify! (fixture/did-open-notification "definition/a.clj"))
  (lsp/notify! (fixture/did-open-notification "definition/b.clj"))

  (testing "common vars"
    (testing "find definition on same ns"
      (h/assert-submap
        {:uri (h/source-path->uri "definition/a.clj")
         :range {:start {:line 8 :character 16}
                 :end {:line 8 :character 33}}}
        (lsp/request! (fixture/definition-request "definition/a.clj" 11 2))))

    (testing "find definition of local var"
      (h/assert-submap
        {:uri (h/source-path->uri "definition/a.clj")
         :range {:start {:line 3 :character 5}
                 :end {:line 3 :character 13}}}
        (lsp/request! (fixture/definition-request "definition/a.clj" 9 9))))

    (testing "find definition of public var"
      (h/assert-submap
        {:uri (h/source-path->uri "definition/a.clj")
         :range {:start {:line 5 :character 6}
                 :end {:line 5 :character 22}}}
        (lsp/request! (fixture/definition-request "definition/b.clj" 3 4)))))

  (testing "keywords"
    (testing "do not find definition of local simple keyword"
      (h/assert-submap
        nil
        (lsp/request! (fixture/definition-request "definition/b.clj" 5 2))))

    (testing "do not find definition of invalid local namespaced keyword"
      (h/assert-submap
        nil
        (lsp/request! (fixture/definition-request "definition/b.clj" 7 2))))

    (testing "find definition of valid other ns namespaced keyword"
      (h/assert-submap
        {:uri (h/source-path->uri "definition/a.clj")
         :range {:start {:line 13 :character 7}
                 :end {:line 13 :character 15}}}
        (lsp/request! (fixture/definition-request "definition/b.clj" 9 2))))

    (testing "find definition of valid other aliased keyword"
      (h/assert-submap
        {:uri (h/source-path->uri "definition/a.clj")
         :range {:start {:line 13 :character 7}
                 :end {:line 13 :character 15}}}
        (lsp/request! (fixture/definition-request "definition/b.clj" 11 2))))))
