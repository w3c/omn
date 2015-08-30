package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor;


/**
 * This module contains a collection of YANG definitions for
 * managing 
 * VNFD.
 * Copyright (c) 2013 IETF Trust and the persons identified as
 * authors of the
 * code. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, 
 * with or
 * without modification, is permitted pursuant to, and subject
 * to the 
 * license terms contained in, the Simplified BSD License
 * set forth in Section 4.c 
 * of the IETF Trusts Legal Provisions
 * Relating to IETF 
 * Documents
 * (http://trustee.ietf.org/license-info).
 * 
 * <p>This class represents the following YANG schema fragment defined in module <b>ietf-vnfd</b>
 * <br />Source path: <i>META-INF\yang\ietf-vnfd.yang</i>):
 * <pre>
 * module ietf-vnfd {
 *     yang-version 1;
 *     namespace "urn:ietf:params:xml:ns:yang:ietf-vnfd";
 *     prefix "vnfd";
 * 
 *     revision 2014-01-03 {
 *         description "This module contains a collection of YANG definitions for
 *         managing 
 *                     VNFD.
 *         Copyright (c) 2013 IETF Trust and the persons identified as
 *         authors of the
 *                     code. All rights reserved.
 *         
 *         Redistribution and use in source and binary forms, 
 *                     with or
 *         without modification, is permitted pursuant to, and subject
 *         to the 
 *                     license terms contained in, the Simplified BSD License
 *         set forth in Section 4.c 
 *                     of the IETF Trusts Legal Provisions
 *         Relating to IETF 
 *                     Documents
 *         (http://trustee.ietf.org/license-info).
 *         ";
 *     }
 * 
 *     container VNF-descriptor {
 *         container general-information {
 *             leaf name {
 *                 type string;
 *             }
 *             leaf description {
 *                 type string;
 *             }
 *             leaf vendor {
 *                 type string;
 *             }
 *             leaf version {
 *                 type uint8;
 *             }
 *             leaf sharing {
 *                 type enumeration;
 *             }
 *         }
 *         container deploy-information {
 *             list virtualization-deployment-unit {
 *                 key "index"
 *                 leaf index {
 *                     type uint16;
 *                 }
 *                 container require-resource {
 *                     leaf CPU-unit {
 *                         type uint16;
 *                     }
 *                     leaf memory-size {
 *                         type uint64;
 *                     }
 *                     leaf disk-size {
 *                         type uint64;
 *                     }
 *                 }
 *                 leaf image-ref {
 *                     type string;
 *                 }
 *             }
 *         }
 *         list external-interface {
 *             key "name"
 *             leaf name {
 *                 type string;
 *             }
 *         }
 *     }
 * }
 * </pre>
 */
public interface IetfVnfdData
    extends
    DataRoot
{




    /**
     * A configuration template that describes a VNF.
     */
    VNFDescriptor getVNFDescriptor();

}

