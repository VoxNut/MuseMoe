package com.javaweb.view.navigation;

public interface NavigationListener {
   
    void onNavigationStateChanged(boolean canGoBack, boolean canGoForward);
}