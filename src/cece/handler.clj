(ns cece.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [liberator.core :refer [resource defresource]]
            [liberator.representation :refer [render-map-generic]]
            [hiccup.core :refer [html]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.data.json :as json]))

(def default-media-types
  ["application/json"
   "text/plain"
   "text/html"])

(defmethod render-map-generic "application/json" [data context]
  (json/write-str (conj (:links data) (:properties data))))

(defmethod render-map-generic "text/html" [data context]
  (html [:div
         [:h1 (-> data :class first)]
         [:d1
          (mapcat (fn [[key value]] [[:dt key] [:dd value]])
                  (:properties data))]]))

(defrecord Transaction [amount description date])

(def cvs
  (->Transaction "1.80" "cvs drugs" "2015-12-01"))

(def storage
  (->Transaction "1.80" "a-1 storage" "2015-12-01"))

(def amazon
  (->Transaction "1.80" "amazon.com" "2015-12-01"))

(def transactions [amazon storage cvs])

(defn index-data [ctx]
  transactions)

(defresource index
  :allowed-methods [:options :get :post :delete]
  :available-media-types default-media-types
  :handle-ok index-data)

(defresource transaction [id]
  :allowed-methods [:options :get]
  :available-media-types ["text/html" "application/json"]
  :handle-ok (fn [_]
               {:properties
                (nth transactions (Integer/parseInt id))
                :links [{:rel ["self"]
                         :href (str "/transactions/" id)}
                        {:rel ["listing"]
                         :href "/transactions"}]}))


(defroutes app-routes
  (OPTIONS "/" []
           {:headers {"Allow:" "GET, POST, DELETE, OPTIONS"}})
  (ANY "/" [] index)
  (ANY "/transactions" [] index)
  (OPTIONS "/transactions/:id" []
       {:headers {"Allow:" "GET, OPTIONS"}})
  (ANY "/transactions/:id" [id]
       (transaction id))
  (route/resources "/")
  (route/not-found "Not found"))

(def app
  (wrap-defaults app-routes site-defaults))
