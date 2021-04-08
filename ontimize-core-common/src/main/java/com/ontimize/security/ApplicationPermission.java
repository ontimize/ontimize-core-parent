package com.ontimize.security;

/**
 * Class to define the application permissions.
 *
 * @author Imatia Innovation
 */
public class ApplicationPermission extends AbstractClientPermission {

    public ApplicationPermission(String permissionName, boolean restricted) {
        this.name = permissionName;
        this.restricted = restricted;
    }

    public ApplicationPermission(String permissionName, boolean restricted,
            com.ontimize.util.calendar.TimePeriod period) {
        this.name = permissionName;
        this.restricted = restricted;
        this.period = period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ApplicationPermission) {
            if (this.name.equals(((ApplicationPermission) o).getPermissionName())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

}
